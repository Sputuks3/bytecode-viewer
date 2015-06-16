package the.bytecode.club.bytecodeviewer.decompilers;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import me.konloch.kontainer.io.DiskReader;

import org.objectweb.asm.tree.ClassNode;

import the.bytecode.club.bytecodeviewer.BytecodeViewer;
import the.bytecode.club.bytecodeviewer.Dex2Jar;
import the.bytecode.club.bytecodeviewer.JarUtils;
import the.bytecode.club.bytecodeviewer.MiscUtils;
import the.bytecode.club.bytecodeviewer.ZipUtils;

public class JEBDecompiler extends Decompiler {
	private static File launchDir = new File (".");
	//base path to JEB, change to suit your environment
	private static String sDrv = System.getenv("SystemDrive");
	//full location to JEB jar executable
	private static String jebloc =  JEBDecompiler.getBCVDir() + BytecodeViewer.fs + "jeb-1.5.201404100" + BytecodeViewer.fs + "bin" + BytecodeViewer.fs + "jeb.jar";
	//specify location to custom jre to use here, can set to older or newer
	private static String jre = sDrv+ BytecodeViewer.fs + "Java" +BytecodeViewer.fs+ "jdk1.7.0_21"+BytecodeViewer.fs+"bin"+BytecodeViewer.fs+"java.exe";
	//script file name and contents
	private static final byte[] JEB_SCRIPT_DATA = new byte[]{0x69, 0x6D, 0x70, 0x6F, 0x72, 0x74, 0x20, 0x6A, 0x61, 0x76, 0x61, 0x2E, 0x69, 0x6F, 0x2E, 0x2A, 0x3B, 0x0A, 0x69, 0x6D, 0x70, 0x6F, 0x72, 0x74, 0x20, 0x6A, 0x61, 0x76, 0x61, 0x2E, 0x75, 0x74, 0x69, 0x6C, 0x2E, 0x49, 0x74, 0x65, 0x72, 0x61, 0x74, 0x6F, 0x72, 0x3B, 0x0A, 0x0A, 0x69, 0x6D, 0x70, 0x6F, 0x72, 0x74, 0x20, 0x6A, 0x65, 0x62, 0x2E, 0x61, 0x70, 0x69, 0x2E, 0x2A, 0x3B, 0x0A, 0x69, 0x6D, 0x70, 0x6F, 0x72, 0x74, 0x20, 0x6A, 0x65, 0x62, 0x2E, 0x61, 0x70, 0x69, 0x2E, 0x64, 0x65, 0x78, 0x2E, 0x44, 0x65, 0x78, 0x3B, 0x0A, 0x0A, 0x70, 0x75, 0x62, 0x6C, 0x69, 0x63, 0x20, 0x63, 0x6C, 0x61, 0x73, 0x73, 0x20, 0x44, 0x65, 0x63, 0x6F, 0x6D, 0x70, 0x69, 0x6C, 0x65, 0x43, 0x6C, 0x61, 0x73, 0x73, 0x20, 0x69, 0x6D, 0x70, 0x6C, 0x65, 0x6D, 0x65, 0x6E, 0x74, 0x73, 0x20, 0x49, 0x53, 0x63, 0x72, 0x69, 0x70, 0x74, 0x20, 0x7B, 0x0A, 0x0A, 0x0A, 0x09, 0x4A, 0x65, 0x62, 0x49, 0x6E, 0x73, 0x74, 0x61, 0x6E, 0x63, 0x65, 0x20, 0x6A, 0x65, 0x62, 0x20, 0x3D, 0x20, 0x6E, 0x75, 0x6C, 0x6C, 0x3B, 0x0A, 0x20, 0x20, 0x20, 0x20, 0x0A, 0x09, 0x70, 0x75, 0x62, 0x6C, 0x69, 0x63, 0x20, 0x76, 0x6F, 0x69, 0x64, 0x20, 0x72, 0x75, 0x6E, 0x28, 0x4A, 0x65, 0x62, 0x49, 0x6E, 0x73, 0x74, 0x61, 0x6E, 0x63, 0x65, 0x20, 0x6A, 0x65, 0x62, 0x29, 0x20, 0x7B, 0x0A, 0x09, 0x09, 0x74, 0x68, 0x69, 0x73, 0x2E, 0x6A, 0x65, 0x62, 0x20, 0x3D, 0x20, 0x6A, 0x65, 0x62, 0x3B, 0x0A, 0x09, 0x09, 0x53, 0x74, 0x72, 0x69, 0x6E, 0x67, 0x20, 0x64, 0x69, 0x72, 0x52, 0x6F, 0x6F, 0x74, 0x20, 0x3D, 0x20, 0x6E, 0x75, 0x6C, 0x6C, 0x3B, 0x0A, 0x09, 0x09, 0x53, 0x74, 0x72, 0x69, 0x6E, 0x67, 0x20, 0x66, 0x71, 0x43, 0x6C, 0x61, 0x73, 0x73, 0x4E, 0x61, 0x6D, 0x65, 0x20, 0x3D, 0x20, 0x6E, 0x75, 0x6C, 0x6C, 0x3B, 0x0A, 0x09, 0x09, 0x53, 0x74, 0x72, 0x69, 0x6E, 0x67, 0x20, 0x5B, 0x5D, 0x20, 0x6A, 0x65, 0x62, 0x61, 0x72, 0x67, 0x73, 0x20, 0x3D, 0x20, 0x6A, 0x65, 0x62, 0x2E, 0x67, 0x65, 0x74, 0x53, 0x63, 0x72, 0x69, 0x70, 0x74, 0x41, 0x72, 0x67, 0x75, 0x6D, 0x65, 0x6E, 0x74, 0x73, 0x28, 0x29, 0x3B, 0x0A, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x0A, 0x09, 0x09, 0x6A, 0x65, 0x62, 0x2E, 0x73, 0x65, 0x74, 0x45, 0x6E, 0x67, 0x69, 0x6E, 0x65, 0x4F, 0x70, 0x74, 0x69, 0x6F, 0x6E, 0x28, 0x45, 0x6E, 0x67, 0x69, 0x6E, 0x65, 0x4F, 0x70, 0x74, 0x69, 0x6F, 0x6E, 0x2E, 0x44, 0x45, 0x43, 0x4F, 0x4D, 0x50, 0x5F, 0x50, 0x41, 0x52, 0x53, 0x45, 0x5F, 0x54, 0x52, 0x59, 0x43, 0x41, 0x54, 0x43, 0x48, 0x45, 0x53, 0x2C, 0x20, 0x22, 0x74, 0x72, 0x75, 0x65, 0x22, 0x29, 0x3B, 0x0A, 0x0A, 0x09, 0x09, 0x74, 0x72, 0x79, 0x20, 0x7B, 0x0A, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x09, 0x0A, 0x09, 0x09, 0x44, 0x65, 0x78, 0x20, 0x64, 0x65, 0x78, 0x20, 0x3D, 0x20, 0x6A, 0x65, 0x62, 0x2E, 0x67, 0x65, 0x74, 0x44, 0x65, 0x78, 0x28, 0x29, 0x3B, 0x09, 0x0A, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x0A, 0x09, 0x09, 0x69, 0x66, 0x28, 0x64, 0x65, 0x78, 0x20, 0x3D, 0x3D, 0x20, 0x6E, 0x75, 0x6C, 0x6C, 0x29, 0x0A, 0x09, 0x09, 0x09, 0x74, 0x68, 0x72, 0x6F, 0x77, 0x20, 0x6E, 0x65, 0x77, 0x20, 0x45, 0x78, 0x63, 0x65, 0x70, 0x74, 0x69, 0x6F, 0x6E, 0x28, 0x22, 0x44, 0x65, 0x78, 0x20, 0x6E, 0x6F, 0x74, 0x20, 0x66, 0x6F, 0x75, 0x6E, 0x64, 0x2E, 0x22, 0x29, 0x3B, 0x0A, 0x0A, 0x09, 0x09, 0x69, 0x66, 0x28, 0x6A, 0x65, 0x62, 0x61, 0x72, 0x67, 0x73, 0x2E, 0x6C, 0x65, 0x6E, 0x67, 0x74, 0x68, 0x20, 0x3D, 0x3D, 0x20, 0x32, 0x29, 0x20, 0x7B, 0x0A, 0x09, 0x09, 0x09, 0x64, 0x69, 0x72, 0x52, 0x6F, 0x6F, 0x74, 0x20, 0x3D, 0x20, 0x6A, 0x65, 0x62, 0x61, 0x72, 0x67, 0x73, 0x5B, 0x30, 0x5D, 0x3B, 0x0A, 0x09, 0x09, 0x09, 0x66, 0x71, 0x43, 0x6C, 0x61, 0x73, 0x73, 0x4E, 0x61, 0x6D, 0x65, 0x20, 0x3D, 0x20, 0x6A, 0x65, 0x62, 0x61, 0x72, 0x67, 0x73, 0x5B, 0x31, 0x5D, 0x3B, 0x0A, 0x09, 0x09, 0x09, 0x63, 0x72, 0x65, 0x61, 0x74, 0x65, 0x44, 0x65, 0x63, 0x6F, 0x6D, 0x70, 0x69, 0x6C, 0x65, 0x64, 0x46, 0x69, 0x6C, 0x65, 0x28, 0x64, 0x69, 0x72, 0x52, 0x6F, 0x6F, 0x74, 0x2C, 0x20, 0x66, 0x71, 0x43, 0x6C, 0x61, 0x73, 0x73, 0x4E, 0x61, 0x6D, 0x65, 0x29, 0x3B, 0x0A, 0x09, 0x09, 0x7D, 0x0A, 0x09, 0x09, 0x65, 0x6C, 0x73, 0x65, 0x20, 0x69, 0x66, 0x28, 0x6A, 0x65, 0x62, 0x61, 0x72, 0x67, 0x73, 0x2E, 0x6C, 0x65, 0x6E, 0x67, 0x74, 0x68, 0x20, 0x3D, 0x3D, 0x20, 0x31, 0x29, 0x7B, 0x09, 0x2F, 0x2F, 0x57, 0x61, 0x72, 0x6E, 0x69, 0x6E, 0x67, 0x20, 0x3D, 0x20, 0x74, 0x68, 0x69, 0x73, 0x20, 0x63, 0x61, 0x6E, 0x20, 0x74, 0x61, 0x6B, 0x65, 0x20, 0x61, 0x20, 0x77, 0x68, 0x69, 0x6C, 0x65, 0x0A, 0x09, 0x09, 0x09, 0x64, 0x69, 0x72, 0x52, 0x6F, 0x6F, 0x74, 0x20, 0x3D, 0x20, 0x6A, 0x65, 0x62, 0x61, 0x72, 0x67, 0x73, 0x5B, 0x30, 0x5D, 0x3B, 0x0A, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x0A, 0x09, 0x09, 0x09, 0x49, 0x74, 0x65, 0x72, 0x61, 0x74, 0x6F, 0x72, 0x3C, 0x3F, 0x3E, 0x20, 0x73, 0x69, 0x67, 0x6E, 0x61, 0x74, 0x75, 0x72, 0x65, 0x73, 0x20, 0x3D, 0x20, 0x64, 0x65, 0x78, 0x2E, 0x67, 0x65, 0x74, 0x43, 0x6C, 0x61, 0x73, 0x73, 0x53, 0x69, 0x67, 0x6E, 0x61, 0x74, 0x75, 0x72, 0x65, 0x73, 0x28, 0x74, 0x72, 0x75, 0x65, 0x29, 0x2E, 0x69, 0x74, 0x65, 0x72, 0x61, 0x74, 0x6F, 0x72, 0x28, 0x29, 0x3B, 0x0A, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x0A, 0x09, 0x09, 0x09, 0x77, 0x68, 0x69, 0x6C, 0x65, 0x28, 0x73, 0x69, 0x67, 0x6E, 0x61, 0x74, 0x75, 0x72, 0x65, 0x73, 0x2E, 0x68, 0x61, 0x73, 0x4E, 0x65, 0x78, 0x74, 0x28, 0x29, 0x29, 0x20, 0x7B, 0x0A, 0x09, 0x09, 0x09, 0x09, 0x53, 0x74, 0x72, 0x69, 0x6E, 0x67, 0x20, 0x63, 0x73, 0x69, 0x67, 0x20, 0x3D, 0x20, 0x28, 0x53, 0x74, 0x72, 0x69, 0x6E, 0x67, 0x29, 0x20, 0x73, 0x69, 0x67, 0x6E, 0x61, 0x74, 0x75, 0x72, 0x65, 0x73, 0x2E, 0x6E, 0x65, 0x78, 0x74, 0x28, 0x29, 0x3B, 0x0A, 0x0A, 0x09, 0x09, 0x09, 0x63, 0x72, 0x65, 0x61, 0x74, 0x65, 0x44, 0x65, 0x63, 0x6F, 0x6D, 0x70, 0x69, 0x6C, 0x65, 0x64, 0x46, 0x69, 0x6C, 0x65, 0x28, 0x64, 0x69, 0x72, 0x52, 0x6F, 0x6F, 0x74, 0x2C, 0x20, 0x63, 0x73, 0x69, 0x67, 0x2E, 0x73, 0x75, 0x62, 0x73, 0x74, 0x72, 0x69, 0x6E, 0x67, 0x28, 0x31, 0x2C, 0x20, 0x63, 0x73, 0x69, 0x67, 0x2E, 0x6C, 0x65, 0x6E, 0x67, 0x74, 0x68, 0x28, 0x29, 0x20, 0x2D, 0x20, 0x31, 0x29, 0x29, 0x3B, 0x0A, 0x09, 0x09, 0x09, 0x7D, 0x0A, 0x0A, 0x09, 0x09, 0x7D, 0x0A, 0x09, 0x09, 0x65, 0x6C, 0x73, 0x65, 0x20, 0x7B, 0x0A, 0x09, 0x09, 0x09, 0x74, 0x68, 0x72, 0x6F, 0x77, 0x20, 0x6E, 0x65, 0x77, 0x20, 0x45, 0x78, 0x63, 0x65, 0x70, 0x74, 0x69, 0x6F, 0x6E, 0x28, 0x22, 0x49, 0x6E, 0x76, 0x61, 0x6C, 0x69, 0x64, 0x20, 0x6E, 0x6F, 0x20, 0x6F, 0x66, 0x20, 0x61, 0x72, 0x67, 0x75, 0x6D, 0x65, 0x6E, 0x74, 0x73, 0x2E, 0x20, 0x59, 0x6F, 0x75, 0x20, 0x6D, 0x75, 0x73, 0x74, 0x20, 0x61, 0x74, 0x20, 0x6C, 0x65, 0x61, 0x73, 0x74, 0x20, 0x73, 0x70, 0x65, 0x63, 0x69, 0x66, 0x79, 0x20, 0x74, 0x68, 0x65, 0x20, 0x62, 0x61, 0x73, 0x65, 0x20, 0x64, 0x69, 0x72, 0x65, 0x63, 0x74, 0x6F, 0x72, 0x79, 0x20, 0x74, 0x6F, 0x20, 0x64, 0x65, 0x63, 0x6F, 0x6D, 0x70, 0x69, 0x6C, 0x65, 0x20, 0x74, 0x6F, 0x2E, 0x22, 0x29, 0x3B, 0x0A, 0x09, 0x09, 0x7D, 0x0A, 0x0A, 0x09, 0x09, 0x7D, 0x0A, 0x09, 0x09, 0x63, 0x61, 0x74, 0x63, 0x68, 0x28, 0x45, 0x78, 0x63, 0x65, 0x70, 0x74, 0x69, 0x6F, 0x6E, 0x20, 0x65, 0x78, 0x29, 0x0A, 0x09, 0x09, 0x7B, 0x0A, 0x09, 0x09, 0x09, 0x6A, 0x65, 0x62, 0x2E, 0x70, 0x72, 0x69, 0x6E, 0x74, 0x28, 0x65, 0x78, 0x2E, 0x67, 0x65, 0x74, 0x4D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, 0x28, 0x29, 0x29, 0x3B, 0x0A, 0x09, 0x09, 0x09, 0x65, 0x78, 0x2E, 0x70, 0x72, 0x69, 0x6E, 0x74, 0x53, 0x74, 0x61, 0x63, 0x6B, 0x54, 0x72, 0x61, 0x63, 0x65, 0x28, 0x29, 0x3B, 0x0A, 0x09, 0x09, 0x7D, 0x0A, 0x09, 0x09, 0x66, 0x69, 0x6E, 0x61, 0x6C, 0x6C, 0x79, 0x20, 0x7B, 0x0A, 0x09, 0x09, 0x09, 0x6A, 0x65, 0x62, 0x2E, 0x65, 0x78, 0x69, 0x74, 0x28, 0x29, 0x3B, 0x0A, 0x09, 0x09, 0x7D, 0x0A, 0x0A, 0x09, 0x7D, 0x0A, 0x0A, 0x09, 0x70, 0x72, 0x69, 0x76, 0x61, 0x74, 0x65, 0x20, 0x76, 0x6F, 0x69, 0x64, 0x20, 0x63, 0x72, 0x65, 0x61, 0x74, 0x65, 0x44, 0x65, 0x63, 0x6F, 0x6D, 0x70, 0x69, 0x6C, 0x65, 0x64, 0x46, 0x69, 0x6C, 0x65, 0x28, 0x53, 0x74, 0x72, 0x69, 0x6E, 0x67, 0x20, 0x64, 0x69, 0x72, 0x52, 0x6F, 0x6F, 0x74, 0x2C, 0x20, 0x53, 0x74, 0x72, 0x69, 0x6E, 0x67, 0x20, 0x66, 0x71, 0x43, 0x6C, 0x61, 0x73, 0x73, 0x4E, 0x61, 0x6D, 0x65, 0x29, 0x20, 0x74, 0x68, 0x72, 0x6F, 0x77, 0x73, 0x20, 0x49, 0x4F, 0x45, 0x78, 0x63, 0x65, 0x70, 0x74, 0x69, 0x6F, 0x6E, 0x20, 0x7B, 0x0A, 0x09, 0x09, 0x53, 0x74, 0x72, 0x69, 0x6E, 0x67, 0x20, 0x63, 0x64, 0x65, 0x63, 0x20, 0x3D, 0x20, 0x6A, 0x65, 0x62, 0x2E, 0x64, 0x65, 0x63, 0x6F, 0x6D, 0x70, 0x69, 0x6C, 0x65, 0x43, 0x6C, 0x61, 0x73, 0x73, 0x28, 0x27, 0x4C, 0x27, 0x20, 0x2B, 0x20, 0x66, 0x71, 0x43, 0x6C, 0x61, 0x73, 0x73, 0x4E, 0x61, 0x6D, 0x65, 0x20, 0x2B, 0x20, 0x27, 0x3B, 0x27, 0x29, 0x3B, 0x0A, 0x09, 0x09, 0x69, 0x66, 0x28, 0x63, 0x64, 0x65, 0x63, 0x20, 0x3D, 0x3D, 0x20, 0x6E, 0x75, 0x6C, 0x6C, 0x29, 0x7B, 0x0A, 0x09, 0x09, 0x09, 0x63, 0x64, 0x65, 0x63, 0x20, 0x3D, 0x20, 0x22, 0x2F, 0x2F, 0x20, 0x44, 0x65, 0x63, 0x6F, 0x6D, 0x70, 0x69, 0x6C, 0x61, 0x74, 0x69, 0x6F, 0x6E, 0x20, 0x65, 0x72, 0x72, 0x6F, 0x72, 0x22, 0x3B, 0x0A, 0x09, 0x09, 0x7D, 0x0A, 0x0A, 0x09, 0x09, 0x46, 0x69, 0x6C, 0x65, 0x20, 0x64, 0x65, 0x63, 0x66, 0x69, 0x6C, 0x65, 0x20, 0x3D, 0x20, 0x6E, 0x65, 0x77, 0x20, 0x46, 0x69, 0x6C, 0x65, 0x28, 0x64, 0x69, 0x72, 0x52, 0x6F, 0x6F, 0x74, 0x20, 0x2B, 0x20, 0x62, 0x75, 0x69, 0x6C, 0x64, 0x43, 0x6C, 0x61, 0x73, 0x73, 0x50, 0x61, 0x74, 0x68, 0x28, 0x66, 0x71, 0x43, 0x6C, 0x61, 0x73, 0x73, 0x4E, 0x61, 0x6D, 0x65, 0x29, 0x20, 0x2B, 0x20, 0x22, 0x2E, 0x6A, 0x61, 0x76, 0x61, 0x22, 0x29, 0x3B, 0x0A, 0x09, 0x09, 0x64, 0x65, 0x63, 0x66, 0x69, 0x6C, 0x65, 0x2E, 0x67, 0x65, 0x74, 0x50, 0x61, 0x72, 0x65, 0x6E, 0x74, 0x46, 0x69, 0x6C, 0x65, 0x28, 0x29, 0x2E, 0x6D, 0x6B, 0x64, 0x69, 0x72, 0x73, 0x28, 0x29, 0x3B, 0x0A, 0x0A, 0x09, 0x09, 0x46, 0x69, 0x6C, 0x65, 0x4F, 0x75, 0x74, 0x70, 0x75, 0x74, 0x53, 0x74, 0x72, 0x65, 0x61, 0x6D, 0x20, 0x6F, 0x75, 0x74, 0x20, 0x3D, 0x20, 0x6E, 0x65, 0x77, 0x20, 0x46, 0x69, 0x6C, 0x65, 0x4F, 0x75, 0x74, 0x70, 0x75, 0x74, 0x53, 0x74, 0x72, 0x65, 0x61, 0x6D, 0x28, 0x64, 0x65, 0x63, 0x66, 0x69, 0x6C, 0x65, 0x29, 0x3B, 0x0A, 0x09, 0x09, 0x6F, 0x75, 0x74, 0x2E, 0x77, 0x72, 0x69, 0x74, 0x65, 0x28, 0x63, 0x64, 0x65, 0x63, 0x2E, 0x67, 0x65, 0x74, 0x42, 0x79, 0x74, 0x65, 0x73, 0x28, 0x22, 0x55, 0x54, 0x46, 0x2D, 0x38, 0x22, 0x29, 0x29, 0x3B, 0x0A, 0x09, 0x09, 0x6F, 0x75, 0x74, 0x2E, 0x77, 0x72, 0x69, 0x74, 0x65, 0x28, 0x53, 0x74, 0x72, 0x69, 0x6E, 0x67, 0x2E, 0x66, 0x6F, 0x72, 0x6D, 0x61, 0x74, 0x28, 0x22, 0x5C, 0x6E, 0x5C, 0x6E, 0x2F, 0x2F, 0x20, 0x44, 0x65, 0x63, 0x6F, 0x6D, 0x70, 0x69, 0x6C, 0x65, 0x64, 0x20, 0x62, 0x79, 0x20, 0x4A, 0x45, 0x42, 0x20, 0x76, 0x25, 0x73, 0x22, 0x2C, 0x6A, 0x65, 0x62, 0x2E, 0x67, 0x65, 0x74, 0x53, 0x6F, 0x66, 0x74, 0x77, 0x61, 0x72, 0x65, 0x56, 0x65, 0x72, 0x73, 0x69, 0x6F, 0x6E, 0x28, 0x29, 0x29, 0x2E, 0x67, 0x65, 0x74, 0x42, 0x79, 0x74, 0x65, 0x73, 0x28, 0x29, 0x29, 0x3B, 0x0A, 0x09, 0x09, 0x6F, 0x75, 0x74, 0x2E, 0x63, 0x6C, 0x6F, 0x73, 0x65, 0x28, 0x29, 0x3B, 0x0A, 0x09, 0x7D, 0x0A, 0x0A, 0x09, 0x70, 0x72, 0x69, 0x76, 0x61, 0x74, 0x65, 0x20, 0x53, 0x74, 0x72, 0x69, 0x6E, 0x67, 0x20, 0x62, 0x75, 0x69, 0x6C, 0x64, 0x43, 0x6C, 0x61, 0x73, 0x73, 0x50, 0x61, 0x74, 0x68, 0x28, 0x53, 0x74, 0x72, 0x69, 0x6E, 0x67, 0x20, 0x6F, 0x72, 0x64, 0x69, 0x6E, 0x70, 0x61, 0x74, 0x68, 0x29, 0x20, 0x7B, 0x0A, 0x09, 0x09, 0x72, 0x65, 0x74, 0x75, 0x72, 0x6E, 0x20, 0x6F, 0x72, 0x64, 0x69, 0x6E, 0x70, 0x61, 0x74, 0x68, 0x2E, 0x72, 0x65, 0x70, 0x6C, 0x61, 0x63, 0x65, 0x28, 0x22, 0x2F, 0x22, 0x2C, 0x20, 0x53, 0x79, 0x73, 0x74, 0x65, 0x6D, 0x2E, 0x67, 0x65, 0x74, 0x50, 0x72, 0x6F, 0x70, 0x65, 0x72, 0x74, 0x79, 0x28, 0x22, 0x66, 0x69, 0x6C, 0x65, 0x2E, 0x73, 0x65, 0x70, 0x61, 0x72, 0x61, 0x74, 0x6F, 0x72, 0x22, 0x29, 0x29, 0x3B, 0x09, 0x0A, 0x09, 0x7D, 0x0A, 0x7D, 0x0A};
	private static final String JEB_SCRIPT_NAME = "DecompileClass";
	
	public static File tempDex = null; 

	private static String getBCVDir() {
		try {
			return launchDir.getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return launchDir.toString();
	}
	@Override
	public String decompileClassNode(ClassNode cn, byte[] b) {		
		String s = "Bytecode Viewer Version: " + BytecodeViewer.version + BytecodeViewer.nl + BytecodeViewer.nl + "Please send this to konloch@gmail.com. " + BytecodeViewer.nl + BytecodeViewer.nl;
		String jebScriptName = "";	//change JEB Script name here

		final File tempDirectory = new File(BytecodeViewer.tempDirectory + BytecodeViewer.fs + MiscUtils.randomString(32) + BytecodeViewer.fs);
		tempDirectory.mkdirs();

		//for JEB only - only Dex format accepted
		if(tempDex == null) {
			final File tempJar = new File(BytecodeViewer.tempDirectory + BytecodeViewer.fs + "temp"+MiscUtils.randomString(32)+".jar");
			JarUtils.saveAsJar(BytecodeViewer.getLoadedClasses(), tempJar.getAbsolutePath());
			tempDex = new File(BytecodeViewer.tempDirectory + BytecodeViewer.fs + "dex"+MiscUtils.randomString(32)+".dex");
			Dex2Jar.saveAsDex(tempJar, tempDex);
			tempJar.delete();	//cleanup
		}
		
		BytecodeViewer.sm.blocking = false;
		
		try {
			
			String scrPath = writeJEBScript(jebScriptName);
			
			ByteArrayOutputStream logbytes = new ByteArrayOutputStream();
			
			logbytes.write((new StringBuffer("Process:").append(BytecodeViewer.nl).append(BytecodeViewer.nl).toString().getBytes("UTF-8")));
			
			String log = logbytes.toString("UTF-8");
        
			int exitValue = runProcess(logbytes, scrPath, tempDex.getAbsolutePath(), tempDirectory.getAbsolutePath() + BytecodeViewer.fs, cn.name);
		
			log += BytecodeViewer.nl+BytecodeViewer.nl+"Exit Value is " + exitValue;
			
			s = log;
		
			//if the motherfucker failed this'll fail, aka wont set.
			s = DiskReader.loadAsString(tempDirectory.getAbsolutePath() + BytecodeViewer.fs + cn.name + ".java");

		
		

		} catch(Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			e.printStackTrace();
			s += BytecodeViewer.nl+"Bytecode Viewer Version: " + BytecodeViewer.version + BytecodeViewer.nl + BytecodeViewer.nl + sw.toString();
		}
		tempDirectory.delete();
		
		//tempDex.delete();	
		
		BytecodeViewer.sm.blocking = true;
		
		return s;
	}
	
	private int runProcess(OutputStream os, String... params) throws IOException, InterruptedException {
		int exitValue;
		
		ProcessBuilder pb = new ProcessBuilder(
				jre,
				"-jar",
				jebloc,
				"--automation",
				"--script=" + params[0], //scrPath
				params[1], //tempDex.getAbsolutePath()
				"--",
				params[2], //tempDirectory.getAbsolutePath()
				(params.length == 4)? params[3]: null  //cn.name
			);
		

        Process process = pb.start();
        
        //Read out dir output
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
        	if(os instanceof PrintStream)
        		((PrintStream) os).println(line);
        	else if(os instanceof ByteArrayOutputStream)
        		((ByteArrayOutputStream) os).write(line.getBytes());
        }
        br.close();

        is = process.getErrorStream();
        isr = new InputStreamReader(is);
        br = new BufferedReader(isr);
        while ((line = br.readLine()) != null) {
        	if(os instanceof PrintStream)
        		((PrintStream) os).println(line);
        	else if(os instanceof ByteArrayOutputStream)
        		((ByteArrayOutputStream) os).write(line.getBytes());
        }
        br.close();
        
        exitValue = process.waitFor();
		return exitValue;
	}
	
	private String writeJEBScript(String scriptName) throws IOException {

		if(scriptName == null || scriptName.isEmpty())
			scriptName = JEB_SCRIPT_NAME;
		
		StringBuffer fullScriptPath = new StringBuffer(BytecodeViewer.getBCVDirectory()).append(BytecodeViewer.fs).append("JEB").append(BytecodeViewer.fs).append(scriptName).append(".java");
		String s = fullScriptPath.toString();
		File f = new File(s);
		
		f.getParentFile().mkdirs();
		
		try (FileOutputStream out = new FileOutputStream(f))
		{
						
			if(!scriptName.equals(JEB_SCRIPT_NAME)) {
				String temp = (new String (JEB_SCRIPT_DATA)).replaceAll(JEB_SCRIPT_NAME, scriptName);
				out.write(temp.getBytes());
				return s;
			}
			
			out.write(JEB_SCRIPT_DATA);
			
			f.deleteOnExit();
			
			return s;
		}
		catch(IOException e) {
			throw e;
		}
	}

	@Override
	public void decompileToZip(String zipName) {
		//JOptionPane.showMessageDialog(null, "Method 2 Start");
		String ran = MiscUtils.randomString(32);
		final File tempDirectory = new File(BytecodeViewer.tempDirectory + BytecodeViewer.fs + ran + BytecodeViewer.fs);
		tempDirectory.mkdirs();
		final File tempJar = new File(BytecodeViewer.tempDirectory + BytecodeViewer.fs + "temp.jar");
		JarUtils.saveAsJar(BytecodeViewer.getLoadedClasses(), tempJar.getAbsolutePath());

		
		String jebScriptName = "";	//change JEB Script name here
		
		//for JEB only - only Dex format accepted
		final File tempDex = new File(BytecodeViewer.tempDirectory + BytecodeViewer.fs + "dex"+MiscUtils.randomString(32)+".dex");
		Dex2Jar.saveAsDex(tempJar, tempDex);
				
		tempJar.delete();	//cleanup
		
		BytecodeViewer.sm.blocking = false;
		
		try {
			String scrPath = writeJEBScript(jebScriptName);
	        
	        int exitValue = runProcess(System.out, scrPath, tempJar.getAbsolutePath(), tempDirectory.getAbsolutePath());
	        		
	        System.out.println("Exit Value is " + exitValue);
			
	       // ZipUtils.zipDirectory(tempDirectory, new File(zipName));
	        ZipUtils.zipFolder(tempDirectory.getAbsolutePath(), zipName, ran);
	        
	        

		} catch(Exception e) {
			new the.bytecode.club.bytecodeviewer.api.ExceptionUI(e);
		}
		//tempDirectory.delete();
		tempDex.delete();
		BytecodeViewer.sm.blocking = true;
	}

	@Override
	public void decompileToClass(String className, String classNameSaved) {
		final File tempDirectory = new File(BytecodeViewer.tempDirectory + BytecodeViewer.fs + MiscUtils.randomString(32) + BytecodeViewer.fs);
		tempDirectory.mkdirs();
		final File tempJar = new File(BytecodeViewer.tempDirectory + BytecodeViewer.fs + "temp.jar");
		JarUtils.saveAsJar(BytecodeViewer.getLoadedClasses(), tempJar.getAbsolutePath());
		
		
		String jebScriptName = "";	//change JEB Script name here
		
		//for JEB only - only Dex format accepted
		final File tempDex = new File(BytecodeViewer.tempDirectory + BytecodeViewer.fs + "dex"+MiscUtils.randomString(32)+".dex");
		Dex2Jar.saveAsDex(tempJar, tempDex);
		
		tempJar.delete();	//cleanup
		
		BytecodeViewer.sm.blocking = false;
		try {
			
			String scrPath = writeJEBScript(jebScriptName);
	        
	        int exitValue = runProcess(System.out, scrPath, tempDex.getAbsolutePath(), tempDirectory.getAbsolutePath(), className);
	        System.out.println("Exit Value is " + exitValue);
			
			File f = new File(tempDirectory.getAbsolutePath() + BytecodeViewer.fs + className + ".java");
			f.renameTo(new File(classNameSaved));
			tempDex.delete();
			tempDirectory.delete();
			
		} catch(Exception e) {
			new the.bytecode.club.bytecodeviewer.api.ExceptionUI(e);
		}

	}
	
	

}