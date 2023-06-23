package cn.dioxide.common.extension;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Reflect {
    /**
     * 反射包路径下所有类
     * @param pack 包路径
     * @return 类集合
     */
//    public Set<Class<?>> getClasses(String pack) {
//        if (pack.startsWith(".")) {
//            pack = pack.substring(1);
//        }
//        Set<Class<?>> classes = new LinkedHashSet<>();
//        boolean recursive = true;
//        String packageDirName = pack.replace('.', '/');
//        Enumeration<URL> dirs;
//        try {
//            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
//            while (dirs.hasMoreElements()) {
//                URL url = dirs.nextElement();
//                String protocol = url.getProtocol();
//                if ("file".equals(protocol)) {
//                    // 获取包的物理路径
//                    String filePath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);
//                    // 以文件的方式扫描整个包下的文件 并添加到集合中
//                    findClassesInPackageByFile(pack, filePath, recursive, classes);
//                } else if ("jar".equals(protocol)) {
//                    // 如果是jar包文件
//                    JarFile jar;
//                    try {
//                        // 获取jar
//                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
//                        // 从此jar包 得到一个枚举类
//                        Enumeration<JarEntry> entries = jar.entries();
//                        findClassesInPackageByJar(pack, entries, packageDirName, recursive, classes);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return classes;
//    }

    public Set<Class<?>> getClasses(String pack) {
        Set<Class<?>> classes = new LinkedHashSet();
        boolean recursive = true;
        String packageDirName = pack.replace('.', '/');
        URL reflectURL = Reflect.class.getClassLoader().getResource(packageDirName);
        if (reflectURL != null) {
            try {
                JarFile jar = ((JarURLConnection)reflectURL.openConnection()).getJarFile();
                Enumeration<JarEntry> entries = jar.entries();
                findClassesInPackageByJar(pack, entries, packageDirName, recursive, classes);
            } catch (IOException var7) {
                var7.printStackTrace();
            }
        }

        return classes;
    }

    private void findClassesInPackageByFile(String packageName, String packagePath, boolean recursive, Set<Class<?>> classes) {
        File dir = new File(packagePath);
        if (dir.exists() && dir.isDirectory()) {
            File[] dirFiles = dir.listFiles((filex) -> recursive && filex.isDirectory() || filex.getName().endsWith(".class"));

            if (dirFiles == null)
                throw new RuntimeException("dirFiles is null");

            for(File file : dirFiles) {
                if (file.isDirectory()) {
                    findClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
                } else {
                    String className = file.getName().substring(0, file.getName().length() - 6);

                    try {
                        classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + className));
                    } catch (ClassNotFoundException var12) {
                        var12.printStackTrace();
                    }
                }
            }

        }
    }

    /**
     * Jar包内反射
     * @param packageName 包名
     * @param entries entries
     * @param packageDirName packageDirName
     * @param recursive recursive
     * @param classes classes
     */
    private void findClassesInPackageByJar(String packageName, Enumeration<JarEntry> entries, String packageDirName, boolean recursive, Set<Class<?>> classes) {
        while(entries.hasMoreElements()) {
            JarEntry entry = (JarEntry)entries.nextElement();
            String name = entry.getName();
            if (name.charAt(0) == '/') {
                name = name.substring(1);
            }

            if (name.startsWith(packageDirName)) {
                int idx = name.lastIndexOf(47);
                if (idx != -1) {
                    packageName = name.substring(0, idx).replace('/', '.');
                }

                if ((idx != -1 || recursive) && name.endsWith(".class") && !entry.isDirectory()) {
                    String className = name.substring(packageName.length() + 1, name.length() - 6);

                    try {
                        classes.add(Class.forName(packageName + "." + className));
                    } catch (ClassNotFoundException var10) {
                        var10.printStackTrace();
                    }
                }
            }
        }

    }
}
