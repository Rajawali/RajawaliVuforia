/*==============================================================================
            Copyright (c) 2010-2013 QUALCOMM Austria Research Center GmbH.
            All Rights Reserved.
            Qualcomm Confidential and Proprietary
            
@file 
    SampleUtils.cpp

@brief
    Implementation of class SampleUtils.

==============================================================================*/

#include "SampleUtils.h"

#include <math.h>
#include <stdlib.h>

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>


void
SampleUtils::printMatrix(const float* mat)
{
    for(int r=0; r<4; r++,mat+=4)
        LOG("%7.3f %7.3f %7.3f %7.3f", mat[0], mat[1], mat[2], mat[3]);
}


void
SampleUtils::checkGlError(const char* operation)
{ 
    for (GLint error = glGetError(); error; error = glGetError())
        LOG("after %s() glError (0x%x)", operation, error);
}


void
SampleUtils::translatePoseMatrix(float x, float y, float z, float* matrix)
{
    // Sanity check
    if (!matrix)
        return;

    // matrix * translate_matrix
    matrix[12] += 
        (matrix[0] * x + matrix[4] * y + matrix[8]  * z);
        
    matrix[13] += 
        (matrix[1] * x + matrix[5] * y + matrix[9]  * z);
        
    matrix[14] += 
        (matrix[2] * x + matrix[6] * y + matrix[10] * z);
        
    matrix[15] += 
        (matrix[3] * x + matrix[7] * y + matrix[11] * z);
}


void
SampleUtils::rotatePoseMatrix(float angle, float x, float y, float z,
                              float* matrix)
{
    // Sanity check
    if (!matrix)
        return;

    float rotate_matrix[16];
    SampleUtils::setRotationMatrix(angle, x, y, z, rotate_matrix);
        
    // matrix * scale_matrix
    SampleUtils::multiplyMatrix(matrix, rotate_matrix, matrix);
}


void
SampleUtils::scalePoseMatrix(float x, float y, float z, float* matrix)
{
    // Sanity check
    if (!matrix)
        return;

    // matrix * scale_matrix
    matrix[0]  *= x;
    matrix[1]  *= x;
    matrix[2]  *= x;
    matrix[3]  *= x;
                     
    matrix[4]  *= y;
    matrix[5]  *= y;
    matrix[6]  *= y;
    matrix[7]  *= y;
                     
    matrix[8]  *= z;
    matrix[9]  *= z;
    matrix[10] *= z;
    matrix[11] *= z;
}


void
SampleUtils::multiplyMatrix(float *matrixA, float *matrixB, float *matrixC)
{
    int i, j, k;
    float aTmp[16];

    for (i = 0; i < 4; i++)
    {
        for (j = 0; j < 4; j++)
        {
            aTmp[j * 4 + i] = 0.0;

            for (k = 0; k < 4; k++)
                aTmp[j * 4 + i] += matrixA[k * 4 + i] * matrixB[j * 4 + k];
        }
    }

    for (i = 0; i < 16; i++)
        matrixC[i] = aTmp[i];
}


void
SampleUtils::setRotationMatrix(float angle, float x, float y, float z, 
    float *matrix)
{
    double radians, c, s, c1, u[3], length;
    int i, j;

    radians = (angle * M_PI) / 180.0;

    c = cos(radians);
    s = sin(radians);

    c1 = 1.0 - cos(radians);

    length = sqrt(x * x + y * y + z * z);

    u[0] = x / length;
    u[1] = y / length;
    u[2] = z / length;

    for (i = 0; i < 16; i++)
        matrix[i] = 0.0;

    matrix[15] = 1.0;

    for (i = 0; i < 3; i++)
    {
        matrix[i * 4 + (i + 1) % 3] = u[(i + 2) % 3] * s;
        matrix[i * 4 + (i + 2) % 3] = -u[(i + 1) % 3] * s;
    }

    for (i = 0; i < 3; i++)
    {
        for (j = 0; j < 3; j++)
            matrix[i * 4 + j] += c1 * u[i] * u[j] + (i == j ? c : 0.0);
    }
}


unsigned int
SampleUtils::initShader(unsigned int shaderType, const char* source)
{
    GLuint shader = glCreateShader((GLenum)shaderType);
    if (shader)
    {
        glShaderSource(shader, 1, &source, NULL);
        glCompileShader(shader);
        GLint compiled = 0;
        glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
    
        if (!compiled)
        {
            GLint infoLen = 0;
            glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
            if (infoLen)
            {
                char* buf = (char*) malloc(infoLen);
                if (buf)
                {
                    glGetShaderInfoLog(shader, infoLen, NULL, buf);
                    LOG("Could not compile shader %d: %s", 
                        shaderType, buf);
                    free(buf);
                }
                glDeleteShader(shader);
                shader = 0;
            }
        }
    }
    return shader;
}


unsigned int
SampleUtils::createProgramFromBuffer(const char* vertexShaderBuffer,
                                     const char* fragmentShaderBuffer)
{
    GLuint vertexShader = initShader(GL_VERTEX_SHADER, vertexShaderBuffer);
    if (!vertexShader)
        return 0;    

    GLuint fragmentShader = initShader(GL_FRAGMENT_SHADER,
                                        fragmentShaderBuffer);
    if (!fragmentShader)
        return 0;

    GLuint program = glCreateProgram();
    if (program)
    {
        glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        
        glAttachShader(program, fragmentShader);
        checkGlError("glAttachShader");
        
        glLinkProgram(program);
        GLint linkStatus = GL_FALSE;
        glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
        
        if (linkStatus != GL_TRUE)
        {
            GLint bufLength = 0;
            glGetProgramiv(program, GL_INFO_LOG_LENGTH, &bufLength);
            if (bufLength)
            {
                char* buf = (char*) malloc(bufLength);
                if (buf)
                {
                    glGetProgramInfoLog(program, bufLength, NULL, buf);
                    LOG("Could not link program: %s", buf);
                    free(buf);
                }
            }
            glDeleteProgram(program);
            program = 0;
        }
    }
    return program;
}
