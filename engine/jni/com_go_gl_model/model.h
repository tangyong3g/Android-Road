/*************************************************************/
/*                           MODEL.H                         */
/*                                                           */
/* Purpose: Pure virtual class used for loading and rendering*/
/*          3d models.                                       */
/*      Evan Pipho (evan@codershq.com)                       */
/*                                                           */
/*************************************************************/
#ifndef MODEL_H
#define MODEL_H

#include <stdio.h>

//-------------------------------------------------------------
//                        CMODEL                              -
// author: Evan Pipho (evan@codershq.com)                     -
// date  : Jul 09, 2002                                       -
//-------------------------------------------------------------
class CModel
{
public:
	virtual ~CModel()
	{

	}
	
	//Load the model from file
	virtual bool Load(FILE* f, int len) = 0;

	//Render the model, generally at its initial position
	virtual void Render() = 0;

	virtual void Release() = 0;

};


#endif //MODEL_H
