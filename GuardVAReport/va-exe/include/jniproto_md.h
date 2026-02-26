/*
 * @(#)src/contract/jvm/pfm/jniproto_md.h, classes, xn142, 20040714 1.5.9.1
 * ===========================================================================
 * Licensed Materials - Property of IBM
 * "Restricted Materials of IBM"
 *
 * IBM SDK, Java(tm) 2 Technology Edition, v1.4.2
 * (C) Copyright IBM Corp. 1998, 2004. All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 * ===========================================================================
 */





/* 
 *
 * Change activity:
 *
 * Reason  Date   Origin  Description
 * ------  ----   ------  ----------------------------------------------------
 * 009653  080800 hdngmr  Added XJNICALL definition.
 * 41143.1 260202 hdpsm   Define XJNICALL
 * 050801  100502 hdawt   Added XJNICALLBACK and XJNISCOPE macros
 * 050587  230502 hdrjm   Added XJNICALLBACK_FNPTR macros
 *
 * ===========================================================================
 */
 
/* 
 * Define JNI prototype macros. This module was created for 
 * defect 91 because of the difficulties getting VisualAge to
 * accept JNIEXPORT in prototypes on Windows NT.
 * This module is simpler for Visual C++ since JNIEXPORT can
 * be the same for prototypes and definitions.
 *
 */

/*
 * For MS VC++ the value of JNIEXPORT need not change
 */
#ifndef JNIEXPORT
#define JNIEXPORT __declspec(dllexport)
#define JNIEXPORT_PROTOTYPE JNIEXPORT
#endif

#ifndef JNIIMPORT
#define JNIIMPORT __declspec(dllimport)
#endif

#ifndef JNICALL
#define JNICALL __stdcall
#endif

#define XJNICALL __cdecl                                      /*ibm@9653*/ /*ibm@41143.1*/
#define XJNISCOPE static                                           /*ibm@50801*/
#define XJNICALLBACK                                               /*ibm@50801*/

#define XJNICALLBACK_FNPTR_STRUCT(fnType)                          /*ibm@50578*/
#define XJNICALLBACK_FNPTR_SET(fnPtr)                              /*ibm@50578*/
#define XJNICALLBACK_FNPTR(fnPtr) fnPtr                            /*ibm@50578*/

/* END OF FILE jniproto_md.h */
