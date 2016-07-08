/*************************************************************/
/*                           MS3D.H                          */
/*                                                           */
/* Purpose: Class and data structures to load, render and    */
/*          animate Milkshape 3D MS3D files                  */
/*          (http://www.swissquake.ch/chumbalum-soft)        */
/*      Evan Pipho (evan@codershq.com)                       */
/*                                                           */
/*************************************************************/
#ifndef MS3D_H
#define MS3D_H

//-------------------------------------------------------------
//                       INCLUDES                             -
//-------------------------------------------------------------
#include "math3d/math1.h"
#include "model.h"
#include <GLES/gl.h>
#include <GLES/glext.h>
#include <stdio.h>

#pragma pack(push, packing)
#pragma pack(1)

#ifndef NULL
#define NULL 0
#endif

#define FREE(p) { delete p; p = NULL; }
#define FREEARR(p) { delete [] p; p = NULL; }

//-------------------------------------------------------------
//- SMs3dVertex
//- A single vertex
struct SMs3dVertex
{
	unsigned char m_ucFlags;			//Editor flags, unused for the loader
	CVector3 m_vVert;					//X,Y,Z coordinates
	char m_cBone;						//Bone ID (-1 = no bone)
	unsigned char m_ucUnused;
};

//-------------------------------------------------------------
//- SMs3dTriangle
//- Triangle data structure
struct SMs3dTriangle
{
	unsigned short m_usFlags;			//Editor flags (unused for loader)
	unsigned short m_usVertIndices[3];	//Vertex indices
	CVector3 m_vNormals[3];				//Vertex normals;
	float m_fTexCoords[2][3];			//Texture coordinates
	unsigned char m_ucSmoothing;		//Smoothing group
	unsigned char m_ucGroup;			//Group index
};

//-------------------------------------------------------------
//- SMs3dMesh
//- Group of triangles in the ms3d file
struct SMs3dMesh
{
	unsigned char m_ucFlags;   			//Editor flags again
	char m_cName[32];          			//Name of the mesh
	unsigned short m_usNumTris;			//Number of triangles in the group
	unsigned short * m_uspIndices; 		//Triangle indices
	char m_cMaterial;          			//Material index, -1 = no material

	GLushort* mIndexArray;

	//Let itclean up after itself like usual
	SMs3dMesh()
	{
		m_usNumTris = 0;
		m_cMaterial = -1;
		m_uspIndices = 0;
		mIndexArray = 0;
	}
	~SMs3dMesh()
	{
		m_usNumTris = 0;
		FREEARR(m_uspIndices);
		FREEARR(mIndexArray);
	}
};

//-------------------------------------------------------------
//- SMs3dMaterial
//- Material information for the mesh
struct SMs3dMaterial
{
	char m_cName[32];         //Material name
	float m_fAmbient[4];      //Ambient values
	float m_fDiffuse[4];      //Diffuse values
	float m_fSpecular[4];     //Specular values
	float m_fEmissive[4];     //Emissive values
	float m_fShininess;       //0 - 128
	float m_fTransparency;    //0 - 1
	char m_cMode;             //unused
	char m_cTexture[128];     //Texture map file
	char m_cAlpha[128];       //Alpha map file
//	CImage* m_Texture;
	char* m_Texture;	//XXX:TODO

	SMs3dMaterial()
	{
		m_Texture = 0;
	}

};

//-------------------------------------------------------------
//- SMs3dKeyFrame
//- Rotation/Translation information for joints
struct SMs3dKeyFrame
{
	float m_fTime;     //Time at which keyframe is started
	float m_fParam[3]; //Translation or Rotation values
};

//-------------------------------------------------------------
//- SMs3dJoint
//- Bone Joints for animation
struct SMs3dJoint
{
	//Data from file
	unsigned char m_ucpFlags;             //Editor flags
	char m_cName[32];                     //Bone name
    char m_cParent[32];                   //Parent name
    float m_fRotation[3];                 //Starting rotation
    float m_fPosition[3];                 //Starting position
	unsigned short m_usNumRotFrames;      //Numbee of rotation frames
    unsigned short m_usNumTransFrames;    //Number of translation frames

    SMs3dKeyFrame * m_RotKeyFrames;       //Rotation keyframes
    SMs3dKeyFrame * m_TransKeyFrames;     //Translation keyframes

	//Data not loaded from file
	short m_sParent;                     //Parent joint index

	CMatrix4X4 m_matLocal;
	CMatrix4X4 m_matFinal;
	CMatrix4X4 m_matAbs;

	unsigned short m_usCurRotFrame;
	unsigned short m_usCurTransFrame;

	CBoundingSphere m_BoundingSphere;
	CBoundingBox m_BoundingBox;

	 //Clean up after itself like usual
	SMs3dJoint()
	{
		m_RotKeyFrames = 0;
		m_TransKeyFrames = 0;
		m_usCurRotFrame = 0;
		m_usCurTransFrame = 0;
	}
	~SMs3dJoint()
	{
		m_usNumRotFrames = 0;
		m_usNumTransFrames = 0;
		FREEARR(m_RotKeyFrames);
		FREEARR(m_TransKeyFrames);
	}
};

struct SMs3dWeightInfo
{
	unsigned char m_BoneId[3];	// index of joint or -1, if -1, then that weight is ignored, since subVersion 1
	unsigned char m_Weight[3];	// vertex weight ranging from 0 - 100, since subVersion 1
	// weight[0] is the weight for boneId in ms3d_vertex_t
	// weight[1] is the weight for boneIds[0]
	// weight[2] is the weight for boneIds[1]
	// 100.0f - weight[0] - weight[1] - weight[2] is the weight for boneIds[2]
};

#pragma pack(pop, packing)


struct axiAnimation_t;
struct axiSequence_t;
class CMs3dAnimation;

//-------------------------------------------------------------
//                        CMS3D                               -
// author: Evan Pipho (evan@codershq.com)                     -
// date  : Aug 23, 2002                                       -
//-------------------------------------------------------------
class CMs3d: public CModel
{
public:
	friend class CMs3dAnimation;

	//Load the MS3D file
	bool Load(FILE* f, int uiSize);

	//Constructors
	CMs3d(bool loadWeightInfo = true);

	//Destructor
	virtual ~CMs3d();

	void Release();

	//装载动画
	bool LoadPsa(FILE* f, int uiSize);

	int GetGroupCount() { return m_usNumMeshes;	}

	char* GetTextureNames();

	int GetGroupTextureIndex(int group) { return m_pMeshes[group].m_cMaterial; }

	void RenderGroup(int group, int positionHandle, int texcoordHandle);

	//对整体作平移和旋转, curMat' = curMat * mat，但是关节和顶点位置还未更新计算
	void PreTransform(const CMatrix4X4& mat);

	//对整体作平移和旋转, curMat' = mat * curMat，但是关节和顶点位置还未更新计算
	void PostTransform(const CMatrix4X4& mat);

	//绘制
	void Render();

	//根据名称获取关节的id，返回-1表示没有该名称的id
	int GetBoneIdByName(const char* name);

	//获取关节（和关联顶点）的包围球（局部坐标系中）
	CBoundingSphere GetBoneBoundingSphere(int boneId);

	//计算关节当前的包围球（世界坐标系中）
	void TransformBoneBoundingSphere(int boneId, CBoundingSphere& sphere);

	//获取整体的变换矩阵
	CMatrix4X4& GetTransform();

	//设置整体的变换矩阵
	void SetTransform(const CMatrix4X4& mat);

	SMs3dMesh& GetMesh(unsigned id) { return m_pMeshes[MIN(id, m_usNumMeshes - 1)]; }

	//获取动画序列的数目
	int GetAnimationSequenceCount() const;

	//获取最后一帧相对第一帧的平移量
	CVector3 GetAnimationTranslation(int animId);

	//逐帧修正平移量为translation
	void FixAnimationTranslation(int animId, CVector3 translation);

	//获取最后一帧相对第一帧的旋转量
	CQuaternion GetAnimationRotation(int animId);

	//逐帧修正旋转量为rotation
	void FixAnimationRotation(int animId, CQuaternion rotation);

	void PrintAnimationInfo();

	CBoundingBox GetBoundingBox() { return m_BoundingBox; }

	bool Intersect(const CRay& ray, int axisMask);
	int Intersect(const CRay& ray, int axisMask, int& mainIntersectJoint);

	int GetJointCount() const { return m_usNumJoints; }
	CVector3 GetJointPosition(int i);

	void UpdateBoundingSphere(bool useRootJointTransform = false);

private:
	//Setup joints
	void Setup();

	void AnimateBones(float frameTime, axiSequence_t& sequence,
			const CMatrix4X4* inAnimateMatrix = 0, CMatrix4X4* outAnimateMatrix = 0);

	void AnimateVertexes();

	void ReleaseWeightInfos();

	//Data
	unsigned short m_usNumVerts;
	unsigned short m_usNumTriangles;
	unsigned short m_usNumMeshes;
	unsigned short m_usNumMaterials;
	unsigned short m_usNumJoints;

	SMs3dVertex * m_pVertices;
	SMs3dTriangle * m_pTriangles;
	SMs3dMesh * m_pMeshes;
	SMs3dMaterial * m_pMaterials;
	SMs3dJoint * m_pJoints;

	bool mLoadWeightInfo;
	SMs3dWeightInfo * m_pWeightInfos;
	CVector3* m_pLocalPositions[3];
	bool mVertexBlending;

	CMatrix4X4 m_RootPreMatrix;			// 对整个模型的变换

	CBoundingSphere m_BoundingSphere;
	CBoundingBox m_BoundingBox;

	axiAnimation_t* mAnimation;
	char mName[33];

	GLfloat* mVertexArray;
	GLfloat* mNormalArray;
	GLfloat* mTexCoordArray;
};





#endif //MS3D_H
