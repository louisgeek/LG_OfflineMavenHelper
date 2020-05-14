package com.louisgeek.javaweb;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * AAR/JAR 包生成 SHA1/MD5、POM 等文件
 */
public class OfflineMavenHelper {

    /**
     * Maven库
     */
    public static final String GROUP_ID = "com.github.louisgeek";
    public static final String FILE_PATH = "D:\\lib";

    //D:\lib\xwalk_core_library-23.53.589.4.aar
    public static void main(String[] args) {
        //先通过Jar的SHA1查询 如果不存在则解析Manifest查询
        //D:\lib
        File libFileDir = new File(FILE_PATH);
        //
        if (!libFileDir.exists()) {
            System.out.println("--  libFileDir is not exists  --");
            return;
        }

        //
        String[] GROUP_ID_Arr = GROUP_ID.split("\\.");
        if (GROUP_ID_Arr.length != 3) {
            System.out.println("--  GROUP_ID_Arr is not right  --");
            return;
        }
        //////
        if (libFileDir.listFiles() == null) {
            System.out.println("--  libFileDir.listFiles() is null  --");
            return;
        }
        for (File file : libFileDir.listFiles()) {
            if (file.isDirectory()) {
                continue;
            }
            String libName = file.getName();
            System.out.println("--  " + libName + " --");
            String onlyName = libName.substring(0, libName.lastIndexOf("."));
            String ext = libName.substring(libName.lastIndexOf(".") + 1);
            System.out.println("--onlyName  " + onlyName + " --");
            System.out.println("--ext  " + ext + " --");
            String ARTIFACT_ID = onlyName.substring(0, onlyName.lastIndexOf("-"));
            String VERSION = onlyName.substring(onlyName.lastIndexOf("-") + 1);
            System.out.println("--ARTIFACT_ID  " + ARTIFACT_ID + " --");
            System.out.println("--VERSION  " + VERSION + " --");
            //D:\lib\com\github\louisgeek\xwalk_core_library
            File ARTIFACT_ID_File = new File(libFileDir + File.separator + GROUP_ID_Arr[0] + File.separator + GROUP_ID_Arr[1] + File.separator + GROUP_ID_Arr[2], ARTIFACT_ID);
            if (ARTIFACT_ID_File.exists()) {
//                FileTool.deleteAll(ARTIFACT_ID_File);
                FileTool.deleteAllDirectory(ARTIFACT_ID_File);
            }
            //
            ARTIFACT_ID_File.mkdirs();
            //
            try {
                //D:\lib\com\github\louisgeek\xwalk_core_library\maven-metadata.xml
                File metadataFile = new File(ARTIFACT_ID_File.getAbsolutePath(), "maven-metadata.xml");
                if (!metadataFile.exists()) {
                    metadataFile = createXML_maven_metadata(metadataFile, GROUP_ID, ARTIFACT_ID, VERSION);
                } else {
                    updateXML_maven_metadata(metadataFile, VERSION);
                }
                //D:\lib\com\github\louisgeek\xwalk_core_library\maven-metadata.xml.md5
                saveTextToFile(metadataFile.getAbsolutePath() + ".md5", getCheckSum(metadataFile, "MD5"));
                //D:\lib\com\github\louisgeek\xwalk_core_library\maven-metadata.xml.sha1
                saveTextToFile(metadataFile.getAbsolutePath() + ".sha1", getCheckSum(metadataFile, "SHA1"));

                //D:\lib\com\github\louisgeek\xwalk_core_library\23.53.589.4
                File VERSION_FilePath = new File(ARTIFACT_ID_File, VERSION);
                VERSION_FilePath.mkdirs();
                //D:\lib\com\github\louisgeek\xwalk_core_library\23.53.589.4\xwalk_core_library-23.53.589.4.aar
                File libFile = new File(VERSION_FilePath, libName);
                FileTool.copyFile(file, libFile);
//                FileTool.moveFile(file, VERSION_FilePath.getAbsolutePath());
                //D:\lib\com\github\louisgeek\xwalk_core_library\23.53.589.4\xwalk_core_library-23.53.589.4.aar.md5
                saveTextToFile(libFile.getAbsolutePath() + ".md5", getCheckSum(libFile, "MD5"));
                //D:\lib\com\github\louisgeek\xwalk_core_library\23.53.589.4\xwalk_core_library-23.53.589.4.aar.md5.sha1
                saveTextToFile(libFile.getAbsolutePath() + ".sha1", getCheckSum(libFile, "SHA1"));
                //
                //D:\lib\com\github\louisgeek\xwalk_core_library\23.53.589.4\xwalk_core_library-23.53.589.4.pom
                File pomFile = createXML_pom(VERSION_FilePath.getAbsolutePath(), GROUP_ID, ARTIFACT_ID, VERSION, ext);
                //D:\lib\com\github\louisgeek\xwalk_core_library\23.53.589.4\xwalk_core_library-23.53.589.4.pom.md5
                saveTextToFile(pomFile.getAbsolutePath() + ".md5", getCheckSum(pomFile, "MD5"));
                //D:\lib\com\github\louisgeek\xwalk_core_library\23.53.589.4\xwalk_core_library-23.53.589.4.pom.sha1
                saveTextToFile(pomFile.getAbsolutePath() + ".sha1", getCheckSum(pomFile, "SHA1"));

            } catch (Exception e) {
                e.printStackTrace();
            }
           /* if (!getPomByChecksum(jar).isTextOnly()) {
                System.out.println("<!--  Search by Checksum -->");
                System.out.println(getPomByChecksum(jar).asXML());
            } else if (!getPomByManifest(jar).isTextOnly()) {
                System.out.println("<!--  Search by Manifest -->");
                System.out.println(getPomByManifest(jar).asXML());
            } else {
                System.out.println("<!--  No data was found -->");
            }*/

        }
        System.out.println("--  end --");
    }

    private static void saveTextToFile(String filePath, String text) {
        try {
            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(text);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File createXML_maven_metadata(File file, String groupId, String artifactId, String version) throws Exception {
//得到Document对象
        Document document = DocumentHelper.createDocument();
//创建根节点
        Element rootElement = document.addElement("metadata");
        //创建子元素
        Element groupIdElement = rootElement.addElement("groupId");
        groupIdElement.setText(groupId);
        Element artifactIdElement = rootElement.addElement("artifactId");
        artifactIdElement.setText(artifactId);
        Element versioningElement = rootElement.addElement("versioning");
//设置子元素的属性
//        versioningElement.addAttribute("perid", String.valueOf(per.getPerid()));
//创建子子元素
        Element releaseElement = versioningElement.addElement("release");
        //设置文本数据
        releaseElement.setText(version);
        Element versionsElement = versioningElement.addElement("versions");
        Element versionElement = versionsElement.addElement("version");
        versionElement.setText(version);
        Element lastUpdatedElement = versioningElement.addElement("lastUpdated");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
        lastUpdatedElement.setText(simpleDateFormat.format(new Date()));
//
//设置生成xml的格式
        OutputFormat format = OutputFormat.createPrettyPrint();
        // 设置编码格式
        format.setEncoding("UTF-8");
        //声明后面起新行
        format.setNewLineAfterDeclaration(false);
        //创建XML字符输出流
        XMLWriter writer = new XMLWriter(new FileOutputStream(file), format);
        //设置是否转义，默认使用转义字符
        writer.setEscapeText(false);
        //写出Document对象
        writer.write(document);
        //关闭流
        writer.close();
        //
        return file;
    }


    public static File updateXML_maven_metadata(File file, String version) throws Exception {
//得到Document对象
        SAXReader saxReader = SAXReader.createDefault();
        Document document = saxReader.read(file);
//        Document document = DocumentHelper.parseText("textXml");
        Element rootElement = document.getRootElement();
        Element versioningElement = rootElement.element("versioning");
        Element releaseElement = versioningElement.element("release");
        //创建子元素
        releaseElement.setText(version);
        Element versionsElement = versioningElement.element("versions");
        List<Element> versionElementList = versionsElement.elements("version");
        boolean exist = false;
        for (Element versionElement : versionElementList) {
            if (version.equals(versionElement.getText())) {
                exist = true;
                break;
            }
        }
        if (!exist) {
            Element versionElement = versionsElement.addElement("version");
            versionElement.setText(version);
            //
            Element lastUpdatedElement = versioningElement.element("lastUpdated");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
            lastUpdatedElement.setText(simpleDateFormat.format(new Date()));
        }
//
//设置生成xml的格式
        OutputFormat format = OutputFormat.createPrettyPrint();
        // 设置编码格式
        format.setEncoding("UTF-8");
        //声明后面起新行
        format.setNewLineAfterDeclaration(false);
        //创建XML字符输出流
        XMLWriter writer = new XMLWriter(new FileOutputStream(file), format);
        //设置是否转义，默认使用转义字符
        writer.setEscapeText(false);
        //写出Document对象
        writer.write(document);
        //关闭流
        writer.close();
        //
        return file;
    }

    public static File createXML_pom(String filePath, String groupId, String artifactId, String version, String ext) throws Exception {
        File file;
//得到Document对象
        Document document = DocumentHelper.createDocument();
//创建根节点
//        Element rootElement = document.addElement("project");
        //这个无效
//        rootElement.addAttribute("xmlns", "http://maven.apache.org/POM/4.0.0");
        //改用这个
        Element rootElement = document.addElement("project", "http://maven.apache.org/POM/4.0.0");
        rootElement.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        rootElement.addAttribute("xsi:schemaLocation", "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd");
        //创建子元素
        Element modelVersionElement = rootElement.addElement("modelVersion");
        modelVersionElement.setText("4.0.0");
        Element groupIdElement = rootElement.addElement("groupId");
        groupIdElement.setText(groupId);
        Element artifactIdElement = rootElement.addElement("artifactId");
        artifactIdElement.setText(artifactId);
//设置子元素的属性
//        versioningElement.addAttribute("perid", String.valueOf(per.getPerid()));
//创建子子元素
        //设置文本数据
        Element versionElement = rootElement.addElement("version");
        versionElement.setText(version);
        Element packaging_versioningElement = rootElement.addElement("packaging");
        packaging_versioningElement.setText(ext);
//
//设置生成xml的格式
        OutputFormat format = OutputFormat.createPrettyPrint();
        // 设置编码格式
        format.setEncoding("UTF-8");
        //声明后面起新行
        format.setNewLineAfterDeclaration(false);
        //创建XML字符输出流
        file = new File(filePath, artifactId + "-" + version + ".pom");
        XMLWriter writer = new XMLWriter(new FileOutputStream(file), format);
        //设置是否转义，默认使用转义字符
        writer.setEscapeText(false);
        //写出Document对象
        writer.write(document);
        //关闭流
        writer.close();
        //
        return file;
    }


    /**
     * 通过Jar Manifest返回Pom dependency
     *
     * @param file
     * @return
     */
  /*  public static Element getPomByManifest(File file) {
        try {
            JarFile jarfile = new JarFile(file);
            Manifest mainmanifest = jarfile.getManifest();
            jarfile.close();
            if (null == mainmanifest) {
                return new DOMElement("dependency");
            }
            String a = null, v = null;
            if (mainmanifest.getMainAttributes().containsKey(new Attributes.Name("Extension-Name"))) {
                a = mainmanifest.getMainAttributes().getValue(new Attributes.Name("Extension-Name"));
            } else if (mainmanifest.getMainAttributes().containsKey(new Attributes.Name("Implementation-Title"))) {
                a = mainmanifest.getMainAttributes().getValue(new Attributes.Name("Implementation-Title"));
            } else if (mainmanifest.getMainAttributes().containsKey(new Attributes.Name("Specification-Title"))) {
                a = mainmanifest.getMainAttributes().getValue(new Attributes.Name("Specification-Title"));
            }
            if (a != null && a.length() != 0) {
                a = a.replace("\"", "").replace(" ", "-");
            }
            if (mainmanifest.getMainAttributes().containsKey(new Attributes.Name("Bundle-Version"))) {
                v = mainmanifest.getMainAttributes().getValue(new Attributes.Name("Bundle-Version"));
            } else if (mainmanifest.getMainAttributes().containsKey(new Attributes.Name("Implementation-Version"))) {
                v = mainmanifest.getMainAttributes().getValue(new Attributes.Name("Implementation-Version"));
            } else if (mainmanifest.getMainAttributes().containsKey(new Attributes.Name("Specification-Version"))) {
                v = mainmanifest.getMainAttributes().getValue(new Attributes.Name("Specification-Version"));
            }
            if (v != null && v.length() != 0) {
                v = v.replace("\"", "").replace(" ", "-");
            }
            String xml = doGet(nexusUrl + "?a=" + a + "&v=" + v);
            return assemblePomElement(xml);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new DOMElement("dependency");
    }
*/

    /**
     * 解析获取的XML 组装dependency
     *
     * @param xml
     * @return
     */
    public static Element assemblePomElement(String xml) {
        Element dependency = new DOMElement("dependency");

        if (xml != null && xml.length() != 0) {
            try {
                Document document = DocumentHelper.parseText(xml);
                Element dataElement = document.getRootElement().element("data");
                if (dataElement.getText() != null && dataElement.getText().length() != 0) {
                    Element artifactElement = dataElement.element("artifact");
                    dependency.add((Element) artifactElement.element("groupId").clone());
                    dependency.add((Element) artifactElement.element("artifactId").clone());
                    dependency.add((Element) artifactElement.element("version").clone());
                }
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }
        return dependency;
    }


    /**
     * 计算CheckSum
     *
     * @param file
     * @param algorithm SHA1 or MD5
     * @return
     */
    private static String getCheckSum(File file, String algorithm) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance(algorithm);
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

}
