package the.bytecode.club.bytecodeviewer.decompilers;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import the.bytecode.club.bytecodeviewer.JarUtils;
import the.bytecode.club.bytecodeviewer.MiscUtils;
import the.bytecode.club.bytecodeviewer.ZipUtils;

/**
 * Krakatau Java Decompiler Wrapper, requires Python 2.7
 * 
 * @author Konloch
 *
 */

public class KrakatauDecompiler extends Decompiler {
	

	public String decompileClassNode(ClassNode cn, byte[] b) {
		
		doRuntimeAndPythonCheck();
		
		String s = "Bytecode Viewer Version: " + BytecodeViewer.version + BytecodeViewer.nl + BytecodeViewer.nl + "Please send this to konloch@gmail.com. " + BytecodeViewer.nl + BytecodeViewer.nl;
		
		final File tempDirectory = new File(BytecodeViewer.tempDirectory + BytecodeViewer.fs + MiscUtils.randomString(32) + BytecodeViewer.fs);
		tempDirectory.mkdirs();
		
			final File tempJar = new File(BytecodeViewer.tempDirectory + BytecodeViewer.fs + "temp"+MiscUtils.randomString(32)+".jar");
			JarUtils.saveAsJar(BytecodeViewer.getLoadedClasses(), tempJar.getAbsolutePath());
		
		
		BytecodeViewer.sm.blocking = false;
		try {
			
			ByteArrayOutputStream logbytes = new ByteArrayOutputStream();
			
			logbytes.write((new StringBuffer("Process:").append(BytecodeViewer.nl).append(BytecodeViewer.nl).toString().getBytes("UTF-8")));
	        
	        int exitValue = runProcess(logbytes, tempJar.getAbsolutePath(), tempDirectory.getAbsolutePath(), cn.name+".class");
	        
	        String log = logbytes.toString("UTF-8");
	        
	        
	        log += BytecodeViewer.nl+BytecodeViewer.nl+"Exit Value is " + exitValue;
			s = log;
	        
			//if the motherfucker failed this'll fail, aka wont set.
			s = DiskReader.loadAsString(tempDirectory.getAbsolutePath() + BytecodeViewer.fs + cn.name + ".java");
			tempDirectory.delete();
			tempJar.delete();
		} catch(Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			e.printStackTrace();
			s += BytecodeViewer.nl+"Bytecode Viewer Version: " + BytecodeViewer.version + BytecodeViewer.nl + BytecodeViewer.nl + sw.toString();
		}
		
		BytecodeViewer.sm.blocking = true;
		
		return s;
	}

	public void decompileToZip(String zipName) {
		
		doRuntimeAndPythonCheck();
		
		String ran = MiscUtils.randomString(32);
		final File tempDirectory = new File(BytecodeViewer.tempDirectory + BytecodeViewer.fs + ran + BytecodeViewer.fs);
		tempDirectory.mkdirs();
		final File tempJar = new File(BytecodeViewer.tempDirectory + BytecodeViewer.fs + "temp.jar");
		JarUtils.saveAsJar(BytecodeViewer.getLoadedClasses(), tempJar.getAbsolutePath());
		
		BytecodeViewer.sm.blocking = false;
		try {
			
	        
	        int exitValue = runProcess(System.out, tempJar.getAbsolutePath(), tempDirectory.getAbsolutePath(), tempJar.getAbsolutePath());
	        		
	        System.out.println("Exit Value is " + exitValue);
			
	       // ZipUtils.zipDirectory(tempDirectory, new File(zipName));
	        ZipUtils.zipFolder(tempDirectory.getAbsolutePath(), zipName, ran);
	        
			//tempDirectory.delete();
			tempJar.delete();
		} catch(Exception e) {
			new the.bytecode.club.bytecodeviewer.api.ExceptionUI(e);
		}
		
		BytecodeViewer.sm.blocking = true;
	}

	public void decompileToClass(String className, String classNameSaved) {
		
		doRuntimeAndPythonCheck();
		
		final File tempDirectory = new File(BytecodeViewer.tempDirectory + BytecodeViewer.fs + MiscUtils.randomString(32) + BytecodeViewer.fs);
		tempDirectory.mkdirs();
		final File tempJar = new File(BytecodeViewer.tempDirectory + BytecodeViewer.fs + "temp.jar");
		JarUtils.saveAsJar(BytecodeViewer.getLoadedClasses(), tempJar.getAbsolutePath());
		
		BytecodeViewer.sm.blocking = false;
		try {
			
	        
	        int exitValue = runProcess(System.out, tempJar.getAbsolutePath(), tempDirectory.getAbsolutePath(), className + ".class");
	        System.out.println("Exit Value is " + exitValue);
			
			File f = new File(tempDirectory.getAbsolutePath() + BytecodeViewer.fs + className + ".java");
			f.renameTo(new File(classNameSaved));
			tempDirectory.delete();
			tempJar.delete();
		} catch(Exception e) {
			new the.bytecode.club.bytecodeviewer.api.ExceptionUI(e);
		}
	}
	
	private int runProcess(OutputStream os, String... params) throws IOException, InterruptedException {
		int exitValue;
		ProcessBuilder pb = new ProcessBuilder(
				BytecodeViewer.python,
				BytecodeViewer.krakatauWorkingDirectory + BytecodeViewer.fs + "decompile.py",
				"-nauto",
				"-path",
				BytecodeViewer.rt+";"+params[0], //tempJar.getAbsolutePath()
				"-out",
				params[1], //tempDirectory.getAbsolutePath()
				params[2]
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
	
	private void doRuntimeAndPythonCheck()
	{
		if(BytecodeViewer.python.equals("")) {
			BytecodeViewer.showMessage("You need to set your Python 2.7 executable path.");
			BytecodeViewer.viewer.pythonC();
		}
		if(BytecodeViewer.rt.equals("")) {
			BytecodeViewer.showMessage("You need to set your JRE RT Library.\r\n(C:\\Program Files (x86)\\Java\\jre7\\lib\\rt.jar)");
			BytecodeViewer.viewer.rtC();
		}
	}

}
