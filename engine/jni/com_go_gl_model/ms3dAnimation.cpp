#include <assert.h>
#include <time.h>
#include "ms3dAnimation.h"
#include "ms3d.h"
#include "unreal.h"

CMs3dAnimation::CMs3dAnimation(CMs3d* model)
{
	mModel = model;
	Init();
}

void CMs3dAnimation::Init()
{
	mStartFrameTime = 0;
	mEndFrameTime = 0;
	mSpeed = 1;
	mAccumalate = false;
	mAccumulateOnce = false;
	mCurSequence = 0;
	mPtrInAnimateMatrix = 0;
	mPtrOutAnimateMatrix = 0;
}

int CMs3dAnimation::Play(const char* animName, bool accumulate, float speed,
		const CMatrix4X4* inAnimateMatrix)
{
	assert(mModel);
	axiAnimation_t* animation = mModel->mAnimation;
	assert(animation);

	for(mCurSequence = 0; mCurSequence < animation->numSequences; ++mCurSequence){
		if(strcmp(animName, animation->sequences[mCurSequence].name) == 0){
			break;
		}
	}

	return Play(mCurSequence, accumulate, speed, inAnimateMatrix);
}

int CMs3dAnimation::Play(int animId, bool accumulate, float speed,
		const CMatrix4X4* inAnimateMatrix)
{
	axiAnimation_t* animation = mModel->mAnimation;
	assert(animation);
	mCurSequence = MAX(animId, 0);
	mCurSequence = MIN(mCurSequence, animation->numSequences - 1);

	mSpeed = MAX(0.01f, speed);
	mAccumalate = accumulate;
	mAccumulateOnce = false;

	axiSequence_t& sequence = animation->sequences[mCurSequence];
	axiBonePose_t* poses = sequence.poses;
	int numFrames = sequence.numFrames;
	float frameRate = sequence.framerate;
	mStartFrameTime = 0;
	mEndFrameTime = numFrames / frameRate;

	mDuration = (mEndFrameTime - mStartFrameTime) * 1000 / mSpeed + 1;
	mPtrInAnimateMatrix = 0;
	// mBindPoseInverseMatrix = inv(T * R)
	CQuaternion q(sequence.poses[0].quat);
	mBindPoseInverseMatrix = q.ToMatrix4();
	mBindPoseInverseMatrix.SetTranslation(sequence.poses[0].position);
	if(inAnimateMatrix){
		mInAnimateMatrix = *inAnimateMatrix;
		mPtrInAnimateMatrix = &mInAnimateMatrix;
		mBindPoseInverseMatrix = mInAnimateMatrix * mBindPoseInverseMatrix;
	}
	mBindPoseInverseMatrix.InvertTranslationAndRotation();
	//如果accumulate为false，其实mPtrOutInAnimateMatrix可以设为空
	//但是为了保证GetAnimateMatrix()得到正确结果（有需要去获取时），还是让它有效
	mPtrOutAnimateMatrix = &mOutAnimateMatrix;

	//Start();
	return (int) mDuration;
}

void CMs3dAnimation::DoAnimate(float normalizedTime)
{
	if(mModel != 0){
		if(mAccumulateOnce){
			mAccumulateOnce = false;
			// 先累积上一周期的变换，再更新骨骼和顶点，保证渲染正确
			AcculateAnimationTransform();
		}
		mCurFrameTime = (mEndFrameTime - mStartFrameTime) * normalizedTime + mStartFrameTime;
		mModel->AnimateBones(mCurFrameTime, mModel->mAnimation->sequences[mCurSequence],
				mPtrInAnimateMatrix, mPtrOutAnimateMatrix);
		mModel->AnimateVertexes();
	}
}

void CMs3dAnimation::OnAnimationStart()
{
}

void CMs3dAnimation::OnAnimationEnd()
{
	if(mModel != 0 && mAccumalate){
		AcculateAnimationTransform();
		mCurFrameTime = mStartFrameTime;
		mModel->AnimateBones(mCurFrameTime, mModel->mAnimation->sequences[mCurSequence],
				mPtrInAnimateMatrix, mPtrOutAnimateMatrix);
		mModel->AnimateVertexes();
	}
}

void CMs3dAnimation::OnAnimationRepeat()
{
	mAccumulateOnce = mAccumalate;
}

int CMs3dAnimation::GetPlayingAnimationId() const
{
	return mCurSequence;
}

float CMs3dAnimation::GetPlayingAnimationFrameTime() const
{
	return mCurFrameTime;
}

void CMs3dAnimation::AcculateAnimationTransform()
{
	mModel->PreTransform(mOutAnimateMatrix * mBindPoseInverseMatrix);
}

CMatrix4X4 CMs3dAnimation::GetAnimateMatrix() const
{
	return mOutAnimateMatrix * mBindPoseInverseMatrix;
}

CMatrix4X4* CMs3dAnimation::GetInAnimateMatrix() const
{
	return mPtrInAnimateMatrix;
}
