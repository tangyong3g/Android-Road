#ifndef MS3D_ANIMATION_H
#define MS3D_ANIMATION_H

#include "math3d/math1.h"

class CMs3d;

class CMs3dAnimation
{
public:
	CMs3dAnimation(CMs3d* model = 0);

	/**
	 * @param accumulate 是否累积动画变换（例如一直向前行走）
	 */
	int Play(const char* animName, bool accumulate = false, float speed = 1,
			const CMatrix4X4* inAnimateMatrix = 0);

	/**
	 * @param accumulate 是否累积动画变换（例如一直向前行走）
	 */
	int Play(int animId, bool accumulate = false, float speed = 1,
			const CMatrix4X4* inAnimateMatrix = 0);

	void DoAnimate(float normalizedTime);

	void OnAnimationStart();

	void OnAnimationEnd();

	void OnAnimationRepeat();

	int GetPlayingAnimationId() const;

	float GetPlayingAnimationFrameTime() const;

	void AcculateAnimationTransform();

	CMatrix4X4 GetAnimateMatrix() const;

	CMatrix4X4* GetInAnimateMatrix() const;
protected:
	CMs3d* mModel;

private:
	void Init();

	float mDuration;
	float mStartFrameTime;
	float mEndFrameTime;
	float mCurFrameTime;
	float mSpeed;
	bool mAccumalate;
	bool mAccumulateOnce;
	int mCurSequence;
	CMatrix4X4 mBindPoseInverseMatrix;	//根骨头的局部矩阵的逆矩阵
	CMatrix4X4 mInAnimateMatrix;	//上一个动画打断时，根骨头的变换矩阵
	CMatrix4X4 mOutAnimateMatrix;	//根骨头当前的变换矩阵
	CMatrix4X4* mPtrInAnimateMatrix;
	CMatrix4X4* mPtrOutAnimateMatrix;
};

#endif	//MS3D_ANIMATION_H
