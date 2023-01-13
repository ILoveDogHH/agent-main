package agent;



import com.jedigames.utils.ErrorMessage;
import com.jedigames.utils.file.FileLog;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import config.Config;
import dingding.ErrorHandler;
import sun.tools.attach.LinuxVirtualMachine;
import utils.AgentFile;

import java.io.IOException;
import java.util.List;

/**
 * API：Agent方式热更新
 */
public class JavaAgent {


    private static String jarPath;
    private static VirtualMachine vm;
    private static String pid;
    public static int serverid = 0;


    public static void main(String[] args) throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
        System.out.println("agent jar start");
        String className = "";
        try {
            Config.initFromJsonFile();
            serverid = Config.getConfigInt("server_id");
            ErrorHandler.init();
            List<String> agentClass = AgentFile.getAgentClass();
            if(agentClass == null){
                throw new Exception("agent.yaml load fail");
            }
            init();
            for(String aclass : agentClass){
                System.out.println(aclass + ":agent start");
                className = aclass;
                vm.loadAgent(jarPath, aclass);
            }
            System.out.println("agent jar success");
        }catch (Exception e){
            //报错发送到钉钉中
            ErrorHandler.onCronErrorOccur(String.format("%s agent error", className)  + ErrorMessage.getErrorDetail(e), e);
        }finally {
            destroy();
        }
    }


    private static void  init() throws IOException, AttachNotSupportedException {
        jarPath = getJarPath();
        // 当前进程pid
        pid = getPid();
        // 连接虚拟机，并attach当前agent的jar包
        //windows使用
//        vm = VirtualMachine.attach(pid);
//        //linux系统使用
        vm = LinuxVirtualMachine.attach(pid);
    }


    /**
     * 获取jar包路径
     *
     * @return
     */
    private static String getJarPath() {
        // 基于jar包中的类定位jar包位置
        return System.getProperty("user.dir") + "/" + "agent-dyn.jar";
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    private static String getPid() throws IOException {
        String path = System.getProperty("user.dir") + "/" + "server_pid_log.log";
       return FileLog._getLog(path);
    }




    private static void destroy() throws IOException {
        if (vm != null) {
            vm.detach();
        }
    }


}
