#pragma once

#include <stdio.h>
#include <windows.h>
#include <winbase.h>
#include <winsvc.h>
#include "include/jni.h"
#include <string>
#include <vector>
#include <psapi.h>
#include <atlbase.h>
#include <time.h>
#include "Splash.h"
#define SECURITY_WIN32
#include "sspi.h"


std::string g_svcName;
std::string g_svcDispName;
std::string g_clsName;
HANDLE hJVMProcess;
char g_szProcName[MAX_PATH];

CSplash splash1(TEXT(".\\Splash-load.bmp"), RGB(128, 128, 128));

#define LOG_FILE "Va.log"



typedef std::vector<std::string> ParamVec;

const char * GetJavaHome();
const char * findJVM(const char * javaHome);
const char * GetJVM();
VOID ReadParams(const char * SectionName,std::vector<std::string> &prmVector);
VOID ClassPathFromParams(std::vector<std::string> &params,std::string &clsPath);
VOID ReadJarDirectory(std::string &JarPath,ParamVec &jarList);
VOID Install();
VOID UnInstall();
DWORD WINAPI RunJVM(LPVOID lpParam);

DWORD WINAPI InvokeMain(LPVOID lpParam);
DWORD WINAPI InvokeShutdown( LPVOID lpParam );

VOID WINAPI ServiceMain(DWORD dwArgc, LPTSTR *lpszArgv);
VOID WINAPI ServiceHandler(DWORD fdwControl);
BOOL SetTheServiceStatus(DWORD dwStatus);

VOID WritePidFile(DWORD dwPid);
DWORD PidFromFile();
BOOL PidFileExists();
VOID RemovePidFile();
VOID EnableDebugPriv();
BOOL OpenExistingJVM();
BOOL ProcessNameIsJVM(HANDLE hJvm);
VOID EraseLogFile();

VOID Log(char* format, ...);

typedef jint (JNICALL *CreateJavaVM)(JavaVM **pvm, VOID **penv, VOID *args);
CreateJavaVM createJVM;

char pPath[MAX_PATH];
HANDLE  hStopEvent;
HINSTANCE hDllInstance;
JavaVM *vm;


CRITICAL_SECTION		lock;


SERVICE_STATUS_HANDLE   hServiceStatusHandle; 
SERVICE_STATUS          ServiceStatus;

VOID tests()
{
#ifdef _DEBUG 
	InvokeMain(NULL);
	std::string jdir="..//lib";
	ParamVec pvec;
	//ReadJarDirectory(jdir,pvec);
	//Install();
	//EnableDebugPriv();
	//WritePidFile(9496);
	//OpenExistingJVM();	
	exit(0);
#endif
}

BOOL init()
{
	char pName[MAX_PATH+1];

	//Change working directory to where module is located. config file is expected there
	GetModuleFileName(NULL,pName,MAX_PATH);
	char *tmp = strrchr(pName,'\\');
	if ( tmp ) { 
		strcpy_s(g_szProcName,tmp+1);
		*(tmp+1) = 0 ;
	}
	SetCurrentDirectory(pName);
	

	hJVMProcess = NULL;

	EnableDebugPriv();

		g_clsName = "com/guardium/gui/AppMain";
		return true;
}



int main(int argc, char* argv[])
{


	Log("main - start\n");
	if ( ! init() ) 
		exit(1);
#ifdef _DEBUG
	//tests();
#endif
	
	ShowWindow(GetConsoleWindow(),SW_HIDE);
	splash1.ShowSplash();
		InvokeMain(NULL);
}


VOID Log(char* format, ...) {
	// write error or other information into log file
	time_t rawtime;
	char  buf[50];
	time ( &rawtime );
	ctime_s( buf, 50, &rawtime );
	char *tmp = strchr(buf,'\n');
	if ( tmp ) *tmp = 0 ;

	
	FILE* pFile = 0;
	va_list ap;
	fopen_s(&pFile, LOG_FILE,"a");
	if(pFile != 0) {
		fprintf(pFile,"%s : ",buf);
		va_start (ap, format);
		vfprintf (pFile, format, ap);
		va_end (ap);
		fclose(pFile);
	}

	
}



VOID SetAppArgs(JNIEnv *env,jobjectArray &applicationArgs)
{

	ParamVec prmVec;
	
	prmVec.push_back("DISCOVERY_SCAN_INTERVAL=1440");
	if ( prmVec.size() ) {
		applicationArgs = env->NewObjectArray((jsize)prmVec.size(), env->FindClass("java/lang/String"), NULL);
		for(unsigned short i=0;i<prmVec.size();i++)
			env->SetObjectArrayElement(applicationArgs,i,env->NewStringUTF(prmVec[i].c_str()));
	}	
}


VOID ClassPathFromParams(std::vector<std::string> &params,std::string &clsPath)
{
	if ( params.size() == 0 ) return;
	clsPath.append("-Djava.class.path=");
	std::vector<std::string>::const_iterator csi;
	for(csi=params.begin();csi!=params.end();csi++) {
		clsPath.append(csi->c_str());
		clsPath.append(";");
	}
}


DWORD WINAPI InvokeMain(LPVOID lpParam) {
	JavaVMInitArgs vm_args;
	
	jint res;
	JNIEnv *env;
	jclass cls;
	jmethodID mid;
	jobjectArray applicationArgs;
	const char *jvmDllPath=NULL;
	const char *javaHome=NULL;
	char javaBinPath[MAX_PATH] ;
	

	//Load the JVM Dll
	javaHome = GetJavaHome();
	if (!javaHome ) {
		splash1.CloseSplash();
		MessageBox(NULL,"Can not find any installed 64 bit Java\nPlease install a 64 bit Java and try again","No 64 bit Java installed",MB_OK|MB_ICONEXCLAMATION);
		exit(0);
	}
	else {
		strcpy_s(javaBinPath, javaHome);
		strcat_s(javaBinPath, "\\bin");
	}

	SetDllDirectory(javaBinPath);

	jvmDllPath = GetJVM();
	hDllInstance = LoadLibrary(jvmDllPath);

	if(hDllInstance != 0 ) { //Succesfully loaded dll
		Log("Loaded %s\n",jvmDllPath);
	}
	else {
		jvmDllPath = findJVM(javaHome);
		hDllInstance = LoadLibrary(jvmDllPath);
		if(hDllInstance != 0) { //Succesfully loaded dll
			Log("Loaded %s\n",jvmDllPath);
		}
		else {
			Log("Failed to load %s : %ld\n",jvmDllPath,GetLastError());
			return FALSE;
		}
	}	

	//resolve the function pointer JNI_CreateJVM
	createJVM = (CreateJavaVM)GetProcAddress(hDllInstance, "JNI_CreateJavaVM");

	ParamVec optVec;
	std::string clsPath;
	optVec.push_back("va.Jar");
	if ( optVec.size() ) {
		std::string JarPath = optVec[0];
		
		ClassPathFromParams(optVec,clsPath);
		
		if ( clsPath.size() == 0 ) {
			Log("Error: can not create CLASSPATH\n");
			return FALSE;
		}
	}
	else {
		Log("Error: can not read CLASSPATH\n");
		return FALSE;
	}
	optVec.clear();
	optVec.push_back("-Djava.library.path=.");

	JavaVMOption *options = new JavaVMOption [optVec.size()+1];
	options[0].optionString = (char*)clsPath.c_str(); // application class path 
	for(unsigned short i=0;i<optVec.size();i++)
		options[i+1].optionString = (char*)optVec[i].c_str();
	vm_args.version = JNI_VERSION_1_4; //JNI Version 1.4 and above
	vm_args.options = options;
	vm_args.nOptions = (jint)optVec.size()+1;
	vm_args.ignoreUnrecognized = JNI_FALSE;
	//Create the JVM
	res = createJVM(&vm, (VOID **)&env, &vm_args);
	if (res < 0)  {
		Log( "Error creating JVM");
		return FALSE;
	}
	//Find the java class
	Log("Finding class: %s\n",g_clsName.c_str());
	cls = env->FindClass(g_clsName.c_str());
	if ( !cls ) {
		Log("Error:Failed to find class!\n");
		return FALSE;
	}
	
	// invoke the main method
    mid = env->GetStaticMethodID(cls, "main", "([Ljava/lang/String;)V");
	
	if ( !mid ) {
		Log("Can not find main method in class!\n");
		return FALSE;
	}

	Log("Invoking main..\n");
	
	
	SetAppArgs(env,applicationArgs);
    env->CallStaticVoidMethod(cls, mid, applicationArgs);
	Sleep(2000);
	splash1.CloseSplash();
	Log("Main invoked..\n");
	//if there is any exception log
	if(env->ExceptionCheck()) {
		Log( "Exception occured..");
		env->ExceptionDescribe();
		return FALSE;
	}
	try { 
	while (1) { 
		if(env->ExceptionCheck()) {
			env->ExceptionDescribe();
			env->ExceptionClear();
		}
		Sleep(1000);
	}
	}
	catch(...){
		env->ExceptionCheck();
	}
	return TRUE;
}


DWORD WINAPI InvokeShutdown( LPVOID lpParam ) {
	JNIEnv *env;
	jclass cls;
	jmethodID mid;

	return 0;

	//Since the JVM was created in a another thread. We have to attach the thread 
	//to JVM before making JVM calls
	vm->AttachCurrentThread((VOID **)&env, 0); 
	cls = env->FindClass(g_clsName.c_str());
	//JVM signature ()V maps to VOID shutdown()
	mid = env->GetStaticMethodID(cls, "shutdown", "()V");

	if(mid == 0) {
		Log( "No Shutdown method implemented by %s, hence shutdown will not be graceful\n", g_clsName.c_str());
		vm->DetachCurrentThread();
		return 0;
	}
	env->CallStaticVoidMethod(cls, mid, 0);
	Log( "Shutdown method called on class %s\n", g_clsName.c_str());
	vm->DetachCurrentThread();
	Log( "Detached thread\n");
	return 0;
}

bool getRTLVer(CONST CHAR *regPath,std::string &foundVer)
{
	HKEY hTempKey = NULL;
	CHAR javaVer[MAX_PATH] = "";
	DWORD	javaVerLen = (sizeof(javaVer))-1;
	LONG lStatus;


	foundVer.empty();
	Log("Opening: %s\n",regPath);
	lStatus = RegOpenKeyEx (HKEY_LOCAL_MACHINE, regPath,0,KEY_READ, &hTempKey);
	if ( lStatus ) Log("Error: %d\n",lStatus);
	if ( lStatus != ERROR_SUCCESS ) 
		return false;
	Log("Found Key - looking for currentVersion\n");
	memset(javaVer,0,sizeof(javaVer));
	lStatus = RegQueryValueEx(hTempKey,"CurrentVersion",NULL,NULL,(LPBYTE)javaVer,&javaVerLen);
	if ( lStatus ) Log("Error: %d\n",lStatus);
	RegCloseKey(hTempKey);
	if ( lStatus != ERROR_SUCCESS ) 
		return false;
	Log("Found version : %s\n",javaVer);
	foundVer = javaVer;
	return true;

}

const char * GetJavaHome()
{
	HKEY	hTempKey;
	LONG	lStatus;
	static char	javaVer[MAX_PATH]="";
	DWORD	javaVerLen = (sizeof(javaVer))-1;
	ParamVec forceJavaHome;

	
	/*if ( forceJavaHome.size() && forceJavaHome[0].length() ) {
		strncpy_s(javaVer,forceJavaHome[0].c_str(),MAX_PATH-1);
		Log("Found JavaHome section in cfg file: using  %s\n",javaVer);
		return javaVer;
	}
	*/
	CONST CHAR sunPath[MAX_PATH] = TEXT("SOFTWARE\\JavaSoft\\Java Runtime Environment");
	CONST CHAR ibmPath[MAX_PATH] = TEXT("SOFTWARE\\IBM\\Java2 Runtime Environment");

	LPCSTR jvmPath = NULL;

	std::string sunVer = "";
	std::string ibmVer = "";

	getRTLVer(sunPath,sunVer);
	getRTLVer(ibmPath,ibmVer);

	if ( sunVer.length() || ibmVer.length()  )  {
		//decide on which to use
		if ( sunVer.length() == 0 ) { 
			jvmPath = ibmPath;
			strcpy_s(javaVer,(sizeof(javaVer))-1,ibmVer.c_str());
		}
		else if ( ibmVer.length() == 0 ) {
			jvmPath = sunPath;
			strcpy_s(javaVer,(sizeof(javaVer))-1,sunVer.c_str());
		}
		else { //This should not really happen, but who knows, might have both installed
			if ( sunVer > ibmVer ) {
				jvmPath = sunPath;
				strcpy_s(javaVer,(sizeof(javaVer))-1,sunVer.c_str());
			}
			else {
				jvmPath = ibmPath;
				strcpy_s(javaVer,(sizeof(javaVer))-1,ibmVer.c_str());
			}
		}
			
			std::string tmpStr = jvmPath;
			tmpStr.append("\\");
			tmpStr.append(javaVer);
			Log("Opening: %s\n",tmpStr.c_str());
			lStatus = RegOpenKeyEx (HKEY_LOCAL_MACHINE, tmpStr.c_str(),0,KEY_READ , &hTempKey);
			if ( ERROR_SUCCESS == lStatus ) {
				javaVerLen = (sizeof(javaVer))-1;
				memset(javaVer,0,sizeof(javaVer));
				//lStatus = RegGetValue(hTempKey,NULL,"RuntimeLib",RRF_RT_REG_SZ,NULL,javaVer,&javaVerLen);
				lStatus = RegQueryValueEx(hTempKey,"JavaHome",NULL,NULL,(LPBYTE)javaVer,&javaVerLen);
				RegCloseKey(hTempKey);
				if ( ERROR_SUCCESS == lStatus ) {
					Log("JavaHome=%s\n",javaVer);
					return javaVer;
				}
			}
			else {
				Log("Failed to open key: %s\n",tmpStr.c_str());
			}
		}
	else {
		Log("Can't find Java registry entry\n");
	}
	return NULL;
}

const char * GetJVM() 
{
	HKEY	hTempKey;
	LONG	lStatus;
	static char	javaVer[MAX_PATH]="";
	DWORD	javaVerLen = (sizeof(javaVer))-1;
	ParamVec forceJVM;

	
	/*if ( forceJVM.size() && forceJVM[0].length() ) {
		strncpy_s(javaVer,forceJVM[0].c_str(),MAX_PATH-1);
		Log("Found RuntimeLib section in cfg file: using  %s\n",javaVer);
		return javaVer;
	}
	*/
	CONST CHAR sunPath[MAX_PATH] = TEXT("SOFTWARE\\JavaSoft\\Java Runtime Environment");
	CONST CHAR ibmPath[MAX_PATH] = TEXT("SOFTWARE\\IBM\\Java2 Runtime Environment");

	LPCSTR jvmPath = NULL;

	std::string sunVer = "";
	std::string ibmVer = "";

	getRTLVer(sunPath,sunVer);
	getRTLVer(ibmPath,ibmVer);

	if ( sunVer.length() || ibmVer.length()  )  {
		//decide on which to use
		if ( sunVer.length() == 0 ) { 
			jvmPath = ibmPath;
			strcpy_s(javaVer,(sizeof(javaVer))-1,ibmVer.c_str());
		}
		else if ( ibmVer.length() == 0 ) {
			jvmPath = sunPath;
			strcpy_s(javaVer,(sizeof(javaVer))-1,sunVer.c_str());
		}
		else { //This should not really happen, but who knows, might have both installed
			if ( sunVer > ibmVer ) {
				jvmPath = sunPath;
				strcpy_s(javaVer,(sizeof(javaVer))-1,sunVer.c_str());
			}
			else {
				jvmPath = ibmPath;
				strcpy_s(javaVer,(sizeof(javaVer))-1,ibmVer.c_str());
			}
		}
			
			std::string tmpStr = jvmPath;
			tmpStr.append("\\");
			tmpStr.append(javaVer);
			lStatus = RegOpenKeyEx (HKEY_LOCAL_MACHINE, tmpStr.c_str(),0,KEY_READ , &hTempKey);
			if ( ERROR_SUCCESS == lStatus ) {
				javaVerLen = (sizeof(javaVer))-1;
				memset(javaVer,0,sizeof(javaVer));
				lStatus = RegGetValue(hTempKey,NULL,"RuntimeLib",RRF_RT_REG_SZ,NULL,javaVer,&javaVerLen);
				RegCloseKey(hTempKey);
				if ( ERROR_SUCCESS == lStatus ) {
					Log("Registry: JVM Path=%s\n",javaVer);
					return javaVer;
				}
			}
			else {
				Log("Failed to open key: %s\n",tmpStr.c_str());
			}
		}
	else {
		Log("Can't find Java registry entry\n");
	}
	return NULL;
}

const char * findJVM(const char * javaHome) {
	static char dllPath[MAX_PATH+20]="";
	char javaBinPath[MAX_PATH] ;
	char  altDllPath[MAX_PATH+20];
	FILE * file;

	strcpy_s(javaBinPath, javaHome);
	strcat_s(javaBinPath, "\\bin");

	
	//check if jvm.dll is in bin\client\ 
	strcpy_s(altDllPath,javaBinPath);
	strcat_s(altDllPath,"\\client\\jvm.dll");
	Log("Checking for %s...\n",altDllPath);
	if((fopen_s(&file,altDllPath,"r"))==0 ){
		fclose(file);
		strcpy_s(dllPath,altDllPath);
		Log("Found jvm.dll in %s \n",dllPath);
		return dllPath;
	}
		
	//check if jvm.dll is in bin\server\ 
	strcpy_s(altDllPath,javaBinPath);
	strcat_s(altDllPath,"\\server\\jvm.dll");
	Log("Checking for %s...\n",altDllPath);
	if((fopen_s(&file,altDllPath,"r"))==0 ){
		fclose(file);
		strcpy_s(dllPath,altDllPath);
		Log("Found jvm.dll in %s \n",dllPath);
		return dllPath;
	}
	
	return NULL;
}



VOID EnableDebugPriv()
{
	HANDLE hToken;
	LUID sedebugnameValue;
	TOKEN_PRIVILEGES tkp;

	OpenProcessToken( GetCurrentProcess(),
	TOKEN_ADJUST_PRIVILEGES | TOKEN_QUERY, &hToken );


	LookupPrivilegeValue( NULL, SE_DEBUG_NAME, &sedebugnameValue );

	tkp.PrivilegeCount = 1;
	tkp.Privileges[0].Luid = sedebugnameValue;
	tkp.Privileges[0].Attributes = SE_PRIVILEGE_ENABLED;

	AdjustTokenPrivileges( hToken, FALSE, &tkp, sizeof tkp, NULL, NULL );

	CloseHandle( hToken );
}

VOID EraseLogFile()
{
	DeleteFile(LOG_FILE);
}