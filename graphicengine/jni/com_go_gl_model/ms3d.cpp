/*************************************************************/
/*                           MS3D.CPP                        */
/*                                                           */
/* Purpose: Implementation of CMs3d for Milkshape 3D MS3D    */
/*          files (http://www.swissquake.ch/chumbalum-soft)  */
/*      Evan Pipho (evan@codershq.com)                       */
/*                                                           */
/*************************************************************/

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#define LOG_LEVEL_E
#include "util/log.h"

#include "ms3d.h"
#include "unreal.h"

#define ROOT_JOINT 0


static void printMatrix(const char* info, CMatrix4X4& mat)
{
	LOGI(info);
	float* val = mat.Get();
	LOGV("%.3f\t%.3f\t%.3f\t%.3f", val[0], val[4], val[ 8], val[12]);
	LOGV("%.3f\t%.3f\t%.3f\t%.3f", val[1], val[5], val[ 9], val[13]);
	LOGV("%.3f\t%.3f\t%.3f\t%.3f", val[2], val[6], val[10], val[14]);
	LOGV("%.3f\t%.3f\t%.3f\t%.3f", val[3], val[7], val[11], val[15]);
}

#define GET_CHAR(src, dst) \
	(dst) = *((src)++);

#define GET_INT(src, dst) \
	(dst) = *((int*)(src)); \
	(src) += 4;

#define GET_COMMENTS(src, num) \
	GET_INT(src, num);
//TODO 跳过num个字符串，因为当前num为0所以可以直接忽略

#define GET_BYTES(src, dst, num) \
	memcpy(dst, src, num); \
	(src) += num;

//-------------------------------------------------------------
//- Load
//- Loads an MS3D file into memory
//-------------------------------------------------------------
void CMs3d::AnimateVertexes()
{
	CVector3 vecVertex;
	CVector3 vecTmp;

	GLfloat* pVertexArray = mVertexArray;
	for(int i = 0; i < m_usNumVerts; ++i){
		SMs3dVertex& vertex = m_pVertices[i];
		if(vertex.m_cBone == (char)0xFF)
		{
			memcpy(pVertexArray, vertex.m_vVert.Get(), sizeof(GLfloat) * 3);
		}
		else
		{
			SMs3dJoint& joint = m_pJoints[vertex.m_cBone];
			vecVertex = vertex.m_vVert;
			vecVertex.Transform4(joint.m_matFinal);
			if(mVertexBlending){
				SMs3dWeightInfo& info = m_pWeightInfos[i];
				float weightSum = info.m_Weight[0];
				if(info.m_BoneId[0] != (unsigned char)0xFF){
					vecVertex *= info.m_Weight[0] * (1.0f / 100.0f);

					SMs3dJoint& joint1 = m_pJoints[info.m_BoneId[0]];
					m_pLocalPositions[0][i].Transform4To(joint1.m_matFinal, vecTmp.Get());
					AccumulateVertex(vecVertex, vecTmp, info.m_Weight[1] * (1.0f / 100.0f));
					weightSum += info.m_Weight[1];
				}
				if(info.m_BoneId[1] != (unsigned char)0xFF){
					SMs3dJoint& joint2 = m_pJoints[info.m_BoneId[1]];
					m_pLocalPositions[1][i].Transform4To(joint2.m_matFinal, vecTmp.Get());
					AccumulateVertex(vecVertex, vecTmp, info.m_Weight[2] * (1.0f / 100.0f));
					weightSum += info.m_Weight[2];
				}
				if(info.m_BoneId[2] != (unsigned char)0xFF){
					SMs3dJoint& joint3 = m_pJoints[info.m_BoneId[2]];
					m_pLocalPositions[2][i].Transform4To(joint3.m_matFinal, vecTmp.Get());
					AccumulateVertex(vecVertex, vecTmp, 1 - weightSum * (1.0f / 100.0f));
				}
			}
			memcpy(pVertexArray, vecVertex.Get(), sizeof(GLfloat) * 3);
		}
		pVertexArray += 3;
	}
}

bool CMs3d::Load(FILE* f, int uiSize)
{
	const char * szFilename = "";	//XXX:TODO
	Release();

	unsigned char * ucpBuffer = 0;
	unsigned char * ucpPtr = 0;


	//Allocate memory
	ucpBuffer = new unsigned char[uiSize];	//XXX:TODO 不需要缓冲
	ucpPtr = ucpBuffer;
	if(!ucpBuffer)
	{
		LOGE("Could not allocate memory");
		return false;
	}
	//Read file into buffer
	if(fread(ucpBuffer, 1, uiSize, f) != uiSize)
	{
		LOGE("Could not read %s", szFilename);
		delete [] ucpBuffer;
		fclose(f);
		return false;
	}
	fclose(f);

	//TODO:为了隐藏模型的类型，修改了文件头，因此在这里不检查文件头
//	//Check out the header, it should be 10 bytes, MS3D000000
//	if(memcmp(ucpPtr, "MS3D000000", 10) != 0)
//	{
//		LOGE("%s is not a valid Milkshape 3D file", szFilename);
//		delete [] ucpBuffer;
//		return false;
//	}
	//Check the version (should be 3 or 4)
	ucpPtr += 10;
	if(*(int *)ucpPtr != 3 && *(int *)ucpPtr != 4)
	{
		LOGE("%s is the wrong version, should be 3 or 4", szFilename);
		delete [] ucpBuffer;
		return false;
	}
	ucpPtr += 4;

	//Read the vertices
	//Number of vertices
	m_usNumVerts = *(unsigned short *)ucpPtr;
	ucpPtr += 2;
	//Allocate memory
	m_pVertices = new SMs3dVertex[m_usNumVerts];
	//Copy the vertices
	memcpy(m_pVertices, ucpPtr, m_usNumVerts * sizeof(SMs3dVertex));
	ucpPtr += m_usNumVerts * sizeof(SMs3dVertex);

	//Read the triangles
	m_usNumTriangles = *(unsigned short *)ucpPtr;
	ucpPtr += 2;
	//Alloc memory for triangles
	m_pTriangles = new SMs3dTriangle[m_usNumTriangles];
	//Copy triangles
	memcpy(m_pTriangles, ucpPtr, m_usNumTriangles * sizeof(SMs3dTriangle));
	ucpPtr += m_usNumTriangles * sizeof(SMs3dTriangle);

	//Load mesh groups
	m_usNumMeshes = *(unsigned short *)ucpPtr;
	ucpPtr += 2;
	//Alloc memory for the mesh data
	m_pMeshes = new SMs3dMesh[m_usNumMeshes];
	//Copy the mesh data
	int x;
	for(x = 0; x < m_usNumMeshes; x++)
	{
		//Copy the first part of the data
		memcpy(&m_pMeshes[x], ucpPtr, 35);
		ucpPtr += 35;
		//Allocate triangle index memory
		m_pMeshes[x].m_uspIndices = new unsigned short[m_pMeshes[x].m_usNumTris];
		//Copy triangle index data, plus the material index
		memcpy(m_pMeshes[x].m_uspIndices, ucpPtr, m_pMeshes[x].m_usNumTris * 2);
		ucpPtr += m_pMeshes[x].m_usNumTris * 2;
		m_pMeshes[x].m_cMaterial = ucpPtr[0];
		ucpPtr ++;
	}

	//Read material information
	m_usNumMaterials = *(unsigned short *)ucpPtr;
	ucpPtr += 2;
	//Alloc memory
	m_pMaterials = new SMs3dMaterial[m_usNumMaterials];
	//Copy material information
	for(x = 0; x < m_usNumMaterials; x++)
	{
		memcpy(&m_pMaterials[x], ucpPtr, 361);
		ucpPtr += 361;
		//XXX:TODO
		//Load the images
//		char* textureFileName = m_pMaterials[x].m_cTexture;
//		if(textureFileName[0] != '\0')
//		{
//			if(textureFileName[0] == '.' && textureFileName[1] == '/'){
//				textureFileName += 2;	//强制去掉前面的"./"
//			}
//			m_pMaterials[x].m_Texture = CTextureManager::GetTexture(textureFileName);
//		}
	}

//	delete dir;

	//Skip some data we do not need
	ucpPtr += 4;
	ucpPtr += 8;

	//Read in joint and animation info
	m_usNumJoints = *(unsigned short *)ucpPtr;
	ucpPtr += 2;
	//Allocate memory
	m_pJoints = new SMs3dJoint[m_usNumJoints];
	//Read in joint info
	for(x = 0; x < m_usNumJoints; x++)
	{
		memcpy(&m_pJoints[x], ucpPtr, 93);
		ucpPtr += 93;
		//Allocate memory
		m_pJoints[x].m_RotKeyFrames = new SMs3dKeyFrame[m_pJoints[x].m_usNumRotFrames];
		m_pJoints[x].m_TransKeyFrames = new SMs3dKeyFrame[m_pJoints[x].m_usNumTransFrames];
		//copy keyframe information
		memcpy(m_pJoints[x].m_RotKeyFrames, ucpPtr, m_pJoints[x].m_usNumRotFrames * sizeof(SMs3dKeyFrame));
		ucpPtr += m_pJoints[x].m_usNumRotFrames * sizeof(SMs3dKeyFrame);
		memcpy(m_pJoints[x].m_TransKeyFrames, ucpPtr, m_pJoints[x].m_usNumTransFrames * sizeof(SMs3dKeyFrame));
		ucpPtr += m_pJoints[x].m_usNumTransFrames * sizeof(SMs3dKeyFrame);

	}

	//读取后续的顶点关联权重信息
	if(mLoadWeightInfo){
		int subVersion;
		int nNumGroupComments;
		int nNumMaterialComments;
		int nNumJointComments;
		int nHasModelComment;
		GET_INT(ucpPtr, subVersion);
		GET_COMMENTS(ucpPtr, nNumGroupComments);
		GET_COMMENTS(ucpPtr, nNumMaterialComments);
		GET_COMMENTS(ucpPtr, nNumJointComments);
		GET_COMMENTS(ucpPtr, nHasModelComment);
		GET_INT(ucpPtr, subVersion);
		if(subVersion == 3){
			m_pWeightInfos = new SMs3dWeightInfo[m_usNumVerts];
			if(m_pWeightInfos){
				mVertexBlending = true;
				for(int i = 0; i < sizeof(m_pLocalPositions) / sizeof(CVector3*); ++i){
					m_pLocalPositions[i] = new CVector3[m_usNumVerts];
					if(!m_pLocalPositions[i]){
						ReleaseWeightInfos();
						mVertexBlending = false;
						break;
					}
				}
				if(mVertexBlending){
					char extra[8];
					for(int i = 0; i < m_usNumVerts; ++i){
						GET_BYTES(ucpPtr, m_pWeightInfos[i].m_BoneId, 3);
						GET_BYTES(ucpPtr, m_pWeightInfos[i].m_Weight, 3);
						GET_BYTES(ucpPtr, extra, 8);
					}
				}
			}
		}
	}

	//File loaded
	delete [] ucpBuffer;

	//Setup joints
	Setup();
//	LOGV("PSX File: %s Loaded", szFilename);
	strncpy(mName, szFilename, sizeof(mName) - 1);
	return true;
}


bool CMs3d::LoadPsa(FILE* f, int uiSize)
{
	unsigned char * ucpBuffer = 0;
	unsigned char * ucpPtr = 0;


	//Allocate memory
	ucpBuffer = new unsigned char[uiSize];
	ucpPtr = ucpBuffer;
	if(!ucpBuffer){
		fclose(f);
		return false;
	}
	//Read file into buffer
	if(fread(ucpBuffer, 1, uiSize, f) != uiSize){
		delete [] ucpBuffer;
		fclose(f);
		return false;
	}
	fclose(f);

	mAnimation = ax2_ReadPSA(ucpBuffer, uiSize);

	delete [] ucpBuffer;

	return mAnimation != 0;
}

char* CMs3d::GetTextureNames() {
	int charCapacity = 128;
	int charCount = 0;
	char* buf = new char[charCapacity + 1];
	for (int i = 0; i < m_usNumMaterials; ++i) {
		char* textureFileName = m_pMaterials[i].m_cTexture;
		if (textureFileName[0] != '\0') {
			if (textureFileName[0] == '.' && textureFileName[1] == '/') {
				textureFileName += 2; //强制去掉前面的"./"
			}
		}
		int len = strlen(textureFileName) + 1;
		if (len > charCapacity - charCount) {
			charCapacity += charCapacity > len ? charCapacity : len;
			char* tmpBuf = new char[charCapacity + 1];
			strcpy(tmpBuf, buf);
			FREEARR(buf);
			buf = tmpBuf;
		}
		strcpy(buf + charCount, textureFileName);
		strcpy(buf + charCount + len - 1, "\n");
		charCount += len;
	}
	return buf;
}

void CMs3d::RenderGroup(int group, int positionHandle, int texcoordHandle)
{
//	glVertexAttribPointer (GLuint indx, GLint size, GLenum type, GLboolean normalized, GLsizei stride, const GLvoid* ptr);
//	glDrawElements (GLenum mode, GLsizei count, GLenum type, const GLvoid* indices);

	glVertexAttribPointer(positionHandle, 3, GL_FLOAT, false, 0, mVertexArray);
	glVertexAttribPointer(texcoordHandle, 2, GL_FLOAT, false, 0, mTexCoordArray);
	glDrawElements(GL_TRIANGLES, m_pMeshes[group].m_usNumTris * 3,
			GL_UNSIGNED_SHORT, m_pMeshes[group].mIndexArray);
}

void CMs3d::AnimateBones(float frameTime, axiSequence_t& sequence,
		const CMatrix4X4* inAnimateMatrix, CMatrix4X4* outAnimateMatrix)
{
	axiBonePose_t* poses = sequence.poses;
	int numFrames = sequence.numFrames;
	int numBones = sequence.numBones;
	float frameRate = sequence.framerate;
	float totalTime = numFrames / frameRate;
	int frame1 = MIN((int)(frameTime * frameRate), numFrames - 1);
	int frame2 = MIN(frame1 + 1, numFrames - 1);
	float interpT = frameTime * frameRate - frame1;
	for(int i = 0; i < numBones; ++i){
		CQuaternion q1(poses[frame1 * numBones + i].quat);
		CQuaternion q2(poses[frame2 * numBones + i].quat);
		CQuaternion qFinal = SLERP(q1, q2, interpT);
		CMatrix4X4 mat = qFinal.ToMatrix4();
		CVector3 t1(poses[frame1 * numBones + i].position);
		CVector3 t2(poses[frame2 * numBones + i].position);
		CVector3 tFinal = Lerp(t1, t2, interpT);
		mat.SetTranslation(tFinal.Get());

		if(i == 0){
			if(inAnimateMatrix){
				mat = *inAnimateMatrix * mat;
			}
			if(outAnimateMatrix){
				*outAnimateMatrix = mat;
			}
			m_pJoints[i].m_matFinal = m_RootPreMatrix * mat;
		}
		else{
			m_pJoints[i].m_matFinal = m_pJoints[m_pJoints[i].m_sParent].m_matFinal * mat;
		}
	}
}

void CMs3d::Render()
{
	//XXX:TODO
//	glEnable(GL_TEXTURE_2D);
//	glVertexPointer(3, GL_FLOAT, 0, mVertexArray);
////	glNormalPointer(GL_FLOAT, 0, mNormalArray);
//	glTexCoordPointer(2, GL_FLOAT, 0, mTexCoordArray);
//	glColorPointer(4, GL_FLOAT, 0, 0);
//
//	for(int x = 0; x < m_usNumMeshes; x++){
//		//Set up materials
//		if(m_pMeshes[x].m_cMaterial >= 0){
//			SMs3dMaterial * pCurMat = &m_pMaterials[m_pMeshes[x].m_cMaterial];
//			CImage* texture = pCurMat->m_Texture;
//			if(!texture->GetTextureId()){
//				continue;
//			}
//			//Set the alpha for transparency
//			pCurMat->m_fDiffuse[3] = pCurMat->m_fTransparency;
//
//			//设置材质时GL_FRONT_AND_BACK在HTC G7上会导致崩溃，改用GL_FRONT
//			glMaterialfv(GL_FRONT, GL_AMBIENT, pCurMat->m_fAmbient);
//			glMaterialfv(GL_FRONT, GL_DIFFUSE, pCurMat->m_fDiffuse);
//			glMaterialfv(GL_FRONT, GL_SPECULAR, pCurMat->m_fSpecular);
//			glMaterialfv(GL_FRONT, GL_EMISSION, pCurMat->m_fEmissive);
//			glMaterialf(GL_FRONT, GL_SHININESS, pCurMat->m_fShininess);
//			if(texture->IsTransparent()){
//				glEnable(GL_BLEND);
//				glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//			}
//			texture->Bind();
//		}
//		else{
//			glDisable(GL_BLEND);
//		}
//		glDrawElements(GL_TRIANGLES, m_pMeshes[x].m_usNumTris * 3,
//				GL_UNSIGNED_SHORT, m_pMeshes[x].mIndexArray);
//	}
//	glDisable(GL_BLEND);
}

//-------------------------------------------------------------
//- Setup
//- Get the joints set up to their beggining positions
//-------------------------------------------------------------
void CMs3d::Setup()
{
	mVertexArray = new GLfloat[m_usNumVerts * 3];
	mTexCoordArray = new GLfloat[m_usNumVerts * 2];
//	mNormalArray = new GLfloat[m_usNumVerts * 3];

	//纹理坐标数组只需要计算一次
	for(int x = 0; x < m_usNumMeshes; x++){
//		CImage* image = m_pMaterials[m_pMeshes[x].m_cMaterial].m_Texture;
//		float wr = image->GetWidthRatio();
//		float hr = image->GetHeightRatio();
		float wr = 1, hr = 1;	//XXX:TODO

		int numTris = m_pMeshes[x].m_usNumTris;
		m_pMeshes[x].mIndexArray = new GLushort[numTris * 3];
		GLushort* pIndexArray = m_pMeshes[x].mIndexArray;
		for(int y = 0; y < numTris; y++){
			SMs3dTriangle * pTri = &m_pTriangles[m_pMeshes[x].m_uspIndices[y]];
			GLushort* indices = pTri->m_usVertIndices;
			*(pIndexArray++) = indices[0];
			*(pIndexArray++) = indices[1];
			*(pIndexArray++) = indices[2];
			//假设同一个顶点的纹理坐标是相同的
			mTexCoordArray[indices[0] * 2] = pTri->m_fTexCoords[0][0] * wr;
			mTexCoordArray[indices[0] * 2 + 1] = pTri->m_fTexCoords[1][0] * hr;
			mTexCoordArray[indices[1] * 2] = pTri->m_fTexCoords[0][1] * wr;
			mTexCoordArray[indices[1] * 2 + 1] = pTri->m_fTexCoords[1][1] * hr;
			mTexCoordArray[indices[2] * 2] = pTri->m_fTexCoords[0][2] * wr;
			mTexCoordArray[indices[2] * 2 + 1] = pTri->m_fTexCoords[1][2] * hr;
		}
	}

	int x;
	//Find the parent joint array indices
	for(x = 0; x < m_usNumJoints; x++)
	{
		//If the bone has a parent
		if(m_pJoints[x].m_cParent[0] != '\0')
		{
			//Compare names of theparent bone of x with the names of all bones
			for(int y = 0; y < m_usNumJoints; y++)
			{
				//A match has been found
				if(strcmp(m_pJoints[y].m_cName, m_pJoints[x].m_cParent) == 0)
				{
					m_pJoints[x].m_sParent = y;
				}
			}
		}
		//The bone has no parent
		else
		{
			m_pJoints[x].m_sParent = -1;
		}
	}
	//Go through each joint
	for(x = 0; x < m_usNumJoints; x++)
	{
		m_pJoints[x].m_matLocal.SetRotation(m_pJoints[x].m_fRotation);
		m_pJoints[x].m_matLocal.SetTranslation(m_pJoints[x].m_fPosition);

		//Set the Abs transformations to the parents transformations, combined with their own local ones
		if(m_pJoints[x].m_sParent != -1)
		{
			m_pJoints[x].m_matFinal = m_pJoints[m_pJoints[x].m_sParent].m_matFinal * m_pJoints[x].m_matLocal;

		}
//		//If there is no parent
		else
		{
			m_pJoints[x].m_matFinal = m_pJoints[x].m_matLocal;
		}
		m_pJoints[x].m_matAbs = m_pJoints[x].m_matFinal;
	}


	float minX = INF, minY = INF, minZ = INF;
	float maxX = -INF, maxY = -INF, maxZ = -INF;
	float bounds[256][6];	//TODO:没有动态申请内存，假设关节数目小于256
	for(int i = 0; i < m_usNumJoints; ++i){
		bounds[i][0] = bounds[i][1] = bounds[i][2] = INF;
		bounds[i][3] = bounds[i][4] = bounds[i][5] = -INF;
	}
	//Go through each vertex
	for(x = 0; x < m_usNumVerts; x++)
	{
		float* pos = m_pVertices[x].m_vVert.Get();
		if(minX > pos[0]) minX = pos[0];
		if(minY > pos[1]) minY = pos[1];
		if(minZ > pos[2]) minZ = pos[2];
		if(maxX < pos[0]) maxX = pos[0];
		if(maxY < pos[1]) maxY = pos[1];
		if(maxZ < pos[2]) maxZ = pos[2];

		//If there is no bone..
		if(m_pVertices[x].m_cBone== -1)
			continue;

		if(mVertexBlending){
			SMs3dWeightInfo* info = &m_pWeightInfos[x];
			for(int i = 0; i < sizeof(m_pLocalPositions) / sizeof(CVector3*); ++i){
				if(info->m_BoneId[i] != (unsigned char)0xFF){
					CMatrix4X4& mat = m_pJoints[info->m_BoneId[i]].m_matAbs;
					mat.InverseTranslateRotateVec(pos, m_pLocalPositions[i][x].Get());
				}
				else
					break;
			}
		}
		int bone = m_pVertices[x].m_cBone;
		CMatrix4X4& mat = m_pJoints[bone].m_matFinal;
		mat.InverseTranslateRotateVec(pos, pos);
		TOMIN(bounds[bone][0], pos[0]);
		TOMIN(bounds[bone][1], pos[1]);
		TOMIN(bounds[bone][2], pos[2]);
		TOMAX(bounds[bone][3], pos[0]);
		TOMAX(bounds[bone][4], pos[1]);
		TOMAX(bounds[bone][5], pos[2]);
	}
	for(int i = 0; i < m_usNumJoints; ++i){
		m_pJoints[i].m_BoundingBox = CBoundingBox(
				bounds[i][0], bounds[i][1], bounds[i][2],
				bounds[i][3], bounds[i][4], bounds[i][5]);
	}

	//计算包围球
	CVector3 center((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2);
	float radius = HYPOT3(maxX - minX, maxY - minY, maxZ - minZ) / 2;
	m_BoundingSphere = CBoundingSphere(center, radius);

	//计算包围盒
	CVector3 lowerCorner(minX, minY, minZ);
	CVector3 upperCorner(maxX, maxY, maxZ);
	m_BoundingBox = CBoundingBox(lowerCorner, upperCorner);


	//暂时没有用到光照，所以不需要计算法向量
//	//Go through the normals and transform them
//	for(x = 0; x < m_usNumTriangles; x++)
//	{
//		SMs3dTriangle * pTri = &m_pTriangles[x];
//
//		//Loop through each index
//		for(int z = 0; z < 3; z++)
//		{
//			//Get the vertex so we can find out what joint we need to use to transform the normal
//			SMs3dVertex * pVert = &m_pVertices[pTri->m_usVertIndices[z]];
//
//			//if it is not attached to a bone, don't do any transforms
//			if(pVert->m_cBone == -1)
//				continue;
//
//			SMs3dJoint * pJoint = &m_pJoints[pVert->m_cBone];
//
//			//Transform the normal
//			pJoint->m_matFinal.InverseRotateVec(pTri->m_vNormals[z].Get());
//
//		}
//	}
	m_RootPreMatrix.Identity();

	AnimateVertexes();
}

//-------------------------------------------------------------
//- Constructors
//- 1. Default contructor
//- 2. Load an ms3d (takes a const char *)
//- 3. Destructor
//-------------------------------------------------------------
CMs3d::CMs3d(bool loadWeightInfo)
{
	m_pVertices = 0;
	m_pTriangles = 0;
	m_pMeshes = 0;
	m_pMaterials = 0;
	m_pJoints = 0;
	m_usNumVerts = 0;
	m_usNumTriangles = 0;
	m_usNumMeshes = 0;
	m_usNumMaterials = 0;
	m_usNumJoints = 0;

	mAnimation = 0;
	mLoadWeightInfo = loadWeightInfo;
	m_pWeightInfos = 0;
	for(int i = 0; i < sizeof(m_pLocalPositions) / sizeof(CVector3*); ++i){
		m_pLocalPositions[i] = 0;
	}
	mVertexBlending = false;
	mName[0] = '\0';

	mVertexArray = 0;
	mNormalArray = 0;
	mTexCoordArray = 0;
}

CMs3d::~CMs3d()
{
	Release();
}

void CMs3d::Release()
{
	//XXX:TODO
//	for(int x = 0; x < m_usNumMeshes; x++){
//		CImage* & image = m_pMaterials[m_pMeshes[x].m_cMaterial].m_Texture;
//		CTextureManager::RemoveTexture(image);
//		image = 0;
//	}
	FREEARR(m_pVertices);
	FREEARR(m_pTriangles);
	FREEARR(m_pMeshes);
	FREEARR(m_pMaterials);
	FREEARR(m_pJoints);
	m_usNumVerts = 0;
	m_usNumTriangles = 0;
	m_usNumMeshes = 0;
	m_usNumMaterials = 0;
	m_usNumJoints = 0;
	ReleaseWeightInfos();
	ax2_FreeAnimation(mAnimation);
	mAnimation = 0;

	FREEARR(mVertexArray);
	FREEARR(mNormalArray);
	FREEARR(mTexCoordArray);
}

void CMs3d::PreTransform(const CMatrix4X4& mat)
{
	m_RootPreMatrix *= mat;
}

void CMs3d::PostTransform(const CMatrix4X4& mat)
{
	m_RootPreMatrix = mat * m_RootPreMatrix;
}

int CMs3d::GetBoneIdByName(const char* name)
{
	int boneId = -1;
	if(name){
		for(boneId = m_usNumJoints - 1; boneId >= 0; --boneId){
			if(strcmp(name, m_pJoints[boneId].m_cName) == 0){
				break;
			}
		}
	}
	return boneId;
}

CBoundingSphere CMs3d::GetBoneBoundingSphere(int boneId)
{
	if(boneId == -1 || boneId >= m_usNumJoints){
		return m_BoundingSphere;
	}
	float minX = INF, minY = INF, minZ = INF;
	float maxX = -INF, maxY = -INF, maxZ = -INF;
	CMatrix4X4& mat = m_pJoints[boneId].m_matFinal;
	for(int i = 0; i < m_usNumVerts; ++i){
		SMs3dVertex* pVert = &m_pVertices[i];
		if(pVert->m_cBone == boneId){
			CVector3 v = pVert->m_vVert;
			v.Transform4(mat);
			float* pos = v.Get();
			if(minX > pos[0]) minX = pos[0];
			if(minY > pos[1]) minY = pos[1];
			if(minZ > pos[2]) minZ = pos[2];
			if(maxX < pos[0]) maxX = pos[0];
			if(maxY < pos[1]) maxY = pos[1];
			if(maxZ < pos[2]) maxZ = pos[2];
		}
	}

	CVector3 center;
	center.Set((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2);
	float radius = HYPOT3(maxX - minX, maxY - minY, maxZ - minZ) / 2;
	mat.InverseTranslateVec(center.Get());
	mat.InverseRotateVec(center.Get());
	CBoundingSphere sphere(center, radius);
	sphere.SetTransform(m_pJoints[boneId].m_matFinal);
	return sphere;
}

void CMs3d::TransformBoneBoundingSphere(int boneId, CBoundingSphere& sphere)
{
	if(boneId < 0 || boneId >= m_usNumJoints){
		sphere.SetTransform(m_RootPreMatrix);
	}
	sphere.SetTransform(m_pJoints[boneId].m_matFinal);
}

CMatrix4X4& CMs3d::GetTransform()
{
	return m_RootPreMatrix;
}

void CMs3d::SetTransform(const CMatrix4X4& mat)
{
	m_RootPreMatrix = mat;
}

int CMs3d::GetAnimationSequenceCount() const
{
	return mAnimation ? mAnimation->numSequences : 0;
}

CVector3 CMs3d::GetAnimationTranslation(int animId)
{
	if(mAnimation && animId >= 0 && animId < mAnimation->numSequences){
		axiSequence_t& seq = mAnimation->sequences[animId];
		int boneCount = seq.numBones;
		float* pos1 = seq.poses[0].position;
		float* pos2 = seq.poses[(seq.numFrames - 1) * boneCount].position;
		return CVector3(pos2[0] - pos1[0], pos2[1] - pos1[1], pos2[2] - pos1[2]);
	}
	else return CVector3(0, 0, 0);
}

void CMs3d::FixAnimationTranslation(int animId, CVector3 translation)
{
	if(mAnimation && animId >= 0 && animId < mAnimation->numSequences){
		axiSequence_t& seq = mAnimation->sequences[animId];
		int frameCount = seq.numFrames;
		int boneCount = seq.numBones;
		for(int i = 0; i < boneCount; ++i){
			int endKey = i + (frameCount - 1) * boneCount;
			translation -= CVector3(seq.poses[endKey].position) - CVector3(seq.poses[i].position);
			if(translation.IsZero()) continue;
			float dx = translation[0] / frameCount;
			float dy = translation[1] / frameCount;
			float dz = translation[2] / frameCount;
			float x = 0, y = 0, z = 0;
			for(int j = i + boneCount; j <= endKey; j += boneCount){
				float* pos = seq.poses[j].position;
				pos[0] += (x += dx);
				pos[1] += (y += dy);
				pos[2] += (z += dz);

			}
			translation.Set(0, 0, 0);
		}
	}
}

CQuaternion CMs3d::GetAnimationRotation(int animId)
{
	if(mAnimation && animId >= 0 && animId < mAnimation->numSequences){
		axiSequence_t& seq = mAnimation->sequences[animId];
		int boneCount = seq.numBones;
		CQuaternion q1(seq.poses[0].quat);
		CQuaternion q2(seq.poses[(seq.numFrames - 1) * boneCount].quat);
		return ~q1 * q2;
	}
	else return CQuaternion();
}

void CMs3d::FixAnimationRotation(int animId, CQuaternion rotation)
{
	if(mAnimation && animId >= 0 && animId < mAnimation->numSequences){
		axiSequence_t& seq = mAnimation->sequences[animId];
		int frameCount = seq.numFrames;
		int boneCount = seq.numBones;
		CQuaternion zero(0, 0, 0, 1);
		for(int i = 0; i < boneCount; ++i){
			int endKey = i + (frameCount - 1) * boneCount;
			CQuaternion q1(seq.poses[i].quat);
			CQuaternion q2(seq.poses[endKey].quat);
			rotation = ~q2 * q1 * rotation;
			if(rotation.IsZero()){
				seq.poses[endKey].quat[0] = seq.poses[i].quat[0];
				seq.poses[endKey].quat[1] = seq.poses[i].quat[1];
				seq.poses[endKey].quat[2] = seq.poses[i].quat[2];
				seq.poses[endKey].quat[3] = seq.poses[i].quat[3];
				continue;
			}
			float dt = 1.0f / frameCount;
			float t = 0;
			for(int j = i + boneCount; j <= endKey; j += boneCount){
				float* quat = seq.poses[j].quat;
				q1.Set(quat);
				t += dt;
				q1 *= j == endKey ? rotation : SLERP(zero, rotation, t);
				quat[0] = q1[0];
				quat[1] = q1[1];
				quat[2] = q1[2];
				quat[3] = q1[3];

			}
			rotation = zero;
		}
	}
}

void CMs3d::PrintAnimationInfo()
{
	LOGFUNC();
	axiAnimation_t* anim = mAnimation;
	for(int i = 0; i < anim->numSequences; ++i){
		int numKeys = anim->numBones * anim->sequences[i].numFrames;
		LOGI("========== %s ============", anim->sequences[i].name);
		for(int j = 0; j < anim->numBones; ++j){
			float* quat1 = anim->sequences[i].poses[j].quat;
			float* pos1 = anim->sequences[i].poses[j].position;
			LOGI("key %3d %.3f %.3f %.3f %.3f / %.3f %.3f %.3f", j, quat1[0], quat1[1], quat1[2], quat1[3], pos1[0], pos1[1], pos1[2]);
			int k = j;
			j += numKeys - anim->numBones;
			float* quat2 = anim->sequences[i].poses[j].quat;
			float* pos2 = anim->sequences[i].poses[j].position;
			LOGI("key %3d %.3f %.3f %.3f %.3f / %.3f %.3f %.3f", j, quat2[0], quat2[1], quat2[2], quat2[3], pos2[0], pos2[1], pos2[2]);
			j = k;
		}
	}
}

void CMs3d::ReleaseWeightInfos()
{
	mVertexBlending = false;
	if(m_pWeightInfos)
	{
		delete [] m_pWeightInfos;
		m_pWeightInfos = 0;
	}
	for(int i = 0; i < sizeof(m_pLocalPositions) / sizeof(CVector3*); ++i){
		delete [] m_pLocalPositions[i];
		m_pLocalPositions[i] = 0;
	}
}

void CMs3d::UpdateBoundingSphere(bool useRootJointTransform)
{
	if(useRootJointTransform){
		CMatrix4X4 mat(true);
		float trans[3];
		m_pJoints[ROOT_JOINT].m_matFinal.GetTranslation(trans);
		mat.SetTranslation(trans);
		m_BoundingSphere.SetTransform(mat);
	}
	else{
		m_BoundingSphere.SetTransform(m_RootPreMatrix);
	}

}

bool CMs3d::Intersect(const CRay& ray, int axisMask)
{
	if(!ray.Intersect(m_BoundingSphere, true)){
		return false;
	}
	for(int i = 0; i < m_usNumJoints; ++i){
		SMs3dJoint& joint = m_pJoints[i];
		CRay ray2 = ray;
		ray2.SetInverseTransform(joint.m_matFinal);
		float t0 = 0, t1 = INF;
		if(ray2.Intersect(joint.m_BoundingBox, t0, t1, axisMask)){
			return true;
		}
	}
	return false;
}


int CMs3d::Intersect(const CRay& ray, int axisMask, int& mainIntersectJoint)
{
	if(!ray.Intersect(m_BoundingSphere, true)){
		return false;
	}
	mainIntersectJoint = 0;
	int intersectCount = 0;
	float mainLen = 0;
	for(int i = 0; i < m_usNumJoints; ++i){
		SMs3dJoint& joint = m_pJoints[i];
		CRay ray2 = ray;
		ray2.SetInverseTransform(joint.m_matFinal);
		float t0 = 0, t1 = ray.mInitLength;
		if(ray2.Intersect(joint.m_BoundingBox, t0, t1, axisMask)){
			++intersectCount;
			if(mainLen < t1 - t0){
				mainLen = t1 - t0;
				mainIntersectJoint = i;
			}
		}
	}
	return intersectCount;
}

CVector3 CMs3d::GetJointPosition(int i)
{
	CVector3 res;
	if(0 <= i && i < m_usNumJoints){
		m_pJoints[i].m_matFinal.GetTranslation(res.Get());
	}
	return res;
}

