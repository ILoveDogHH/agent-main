**项目功能**
----
通过pid绑定的方式进行热更，相比于premain优势不用定时检测上传文件。不需要提前在启动参数中加上jre相关热更启动参数。入侵性更小

**核心代码**
----
重写agent-dyn项目中的agentmain方法。通过inst.redefineClasses(classDefList.toArray(new ClassDefinition[0]))加载的方式进行热更处理

    public static void agentmain(String args, Instrumentation inst) throws IOException, ClassNotFoundException, UnmodifiableClassException {
        LinkedHashMap<String, LinkedHashSet<Class<?>>> redefineMap = Maps.newLinkedHashMap();
        // 1.整理需要重定义的类
        List<String> classArr = Arrays.stream(args.split(",")).collect(Collectors.toList());
        List<ClassDefinition> classDefList = new ArrayList<ClassDefinition>();
        for (String className : classArr) {
                Class<?> c = Class.forName(className);
                String classLocation = c.getProtectionDomain().getCodeSource().getLocation().getPath();
                LinkedHashSet<Class<?>> classSet = redefineMap.computeIfAbsent(classLocation,
                        k -> Sets.newLinkedHashSet());
                classSet.add(c);
        }
        if (!redefineMap.isEmpty()) {
            for (Map.Entry<String, LinkedHashSet<Class<?>>> entry : redefineMap.entrySet()) {
                String classLocation = entry.getKey();
                if (classLocation.endsWith(".jar")) {
                    try (JarFile jf = new JarFile(classLocation)) {
                        for (Class<?> cls : entry.getValue()) {
                            String clazz = cls.getName().replace('.', '/') + ".class";
                            JarEntry je = jf.getJarEntry(clazz);
                            if (je != null) {
                                try (InputStream stream = jf.getInputStream(je)) {
                                    byte[] data = IOUtils.toByteArray(stream);
                                    classDefList.add(new ClassDefinition(cls, data));
                                }
                            } else {
                                throw new IOException("JarEntry " + clazz + " not found");
                            }
                        }
                    }
                } else {
                    File file;
                    for (Class<?> cls : entry.getValue()) {
                        String clazz = cls.getName().replace('.', '/') + ".class";
                        file = new File(classLocation, clazz);
                        byte[] data = FileUtils.readFileToByteArray(file);
                        classDefList.add(new ClassDefinition(cls, data));
                    }
                }
            }
            // 2.redefine
            inst.redefineClasses(classDefList.toArray(new ClassDefinition[0]));
        }
    }
    

## **热更步骤** ##
 
-- 将agent-dyn和agent-main打jar包放在server同级中(后续无需再次打包)
 1. 将需要热更的项目（server.jar）上传
 2. 上传热更class类名到agentconfig文件下，格式为.yaml
 3. 执行 java -jar agent-with-dependencies热更
 4. agent-dyn 和 agent-with-dependencies，agentconfig 需要放在server.jar同级中

**返回结果**
----
    agent jar start
    Thread.job.SecondJob:agent start
    agent jar success
  

**注意事项**
----

 1. 只能够热更方法内的改动
 2. 不能够删除，增加方法。不能够修改静态变量
 

**开发者注意事项**
-------

 1. agent-with-dependences.jar依赖于agent-dyn包，使用前需要将agent-dyn包单独打包并且在 agent-with-dependences包中的pom中带有依赖
 2. agent-with-dependences包中需要引用com.sun.tools文件。win和linux中的tools使用环境不同。开发环境中使用windows中的jdk tools，线上环境需要使用linux中的jdk tools
 3. 只能够修改线上环境方法体内的方法改动
 4. agentconfig 文件中需要自己填写需要热更的类名，采用yaml格式

    

    
    
