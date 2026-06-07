package top.niunaijun.blackbox.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Shell命令执行工具类。
 * <p>
 * 提供通过Runtime.exec()执行Shell命令的能力，支持普通用户和Root权限执行。
 * 执行结果封装在 {@link CommandResult} 中，包含退出码和输出信息。
 * <p>
 * 注意：此类为纯工具类，构造函数私有，禁止实例化。
 */
public class ShellUtils {
    /** su命令（获取Root权限） */
    public static final String COMMAND_SU = "su";
    /** sh命令（普通Shell） */
    public static final String COMMAND_SH = "sh";
    /** exit命令 */
    public static final String COMMAND_EXIT = "exit\n";
    /** 命令行结束符 */
    public static final String COMMAND_LINE_END = "\n";


    private ShellUtils() {
        throw new AssertionError();
    }


    /**
     * 检查是否具有Root权限。
     *
     * @return 有Root权限返回true
     */
    public static boolean checkRootPermission() {
        return execCommand("echo root", true, false).result == 0;
    }


    /**
     * 执行单条Shell命令，返回结果信息。
     *
     * @param command 命令字符串
     * @param isRoot  是否以Root权限执行
     * @return 命令执行结果
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(String command, boolean isRoot) {
        return execCommand(new String[]{command}, isRoot, true);
    }


    /**
     * 执行多条Shell命令（列表形式），返回结果信息。
     *
     * @param commands 命令列表
     * @param isRoot   是否以Root权限执行
     * @return 命令执行结果
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(List<String> commands, boolean isRoot) {
        return execCommand(commands == null ? null : commands.toArray(new String[]{}), isRoot, true);
    }

    /**
     * 执行多条Shell命令（数组形式），返回结果信息。
     *
     * @param commands 命令数组
     * @param isRoot   是否以Root权限执行
     * @return 命令执行结果
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot) {
        return execCommand(commands, isRoot, true);
    }


    /**
     * 执行单条Shell命令。
     *
     * @param command         命令字符串
     * @param isRoot          是否以Root权限执行
     * @param isNeedResultMsg 是否需要返回执行结果信息
     * @return 命令执行结果
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(String command, boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(new String[]{command}, isRoot, isNeedResultMsg);
    }


    /**
     * execute shell commands
     *
     * @param commands        command list
     * @param isRoot          whether need to run with root
     * @param isNeedResultMsg whether need result msg
     * @return
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    /**
     * 执行多条Shell命令（列表形式）。
     *
     * @param commands        命令列表
     * @param isRoot          是否以Root权限执行
     * @param isNeedResultMsg 是否需要返回执行结果信息
     * @return 命令执行结果
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(List<String> commands, boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(commands == null ? null : commands.toArray(new String[]{}), isRoot, isNeedResultMsg);
    }

    /**
     * 执行Shell命令数组的核心方法。
     * <p>
     * 通过Runtime.exec()创建Shell进程，依次写入命令并执行。
     * 如果isRoot为true，使用su命令获取Root权限。
     *
     * @param commands        命令数组
     * @param isRoot          是否以Root权限执行
     * @param isNeedResultMsg 是否需要返回执行结果信息
     * @return 命令执行结果，result为-1表示可能有异常
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot, boolean isNeedResultMsg) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null);
        }
        Process process = null;
        BufferedReader successResult = null;
        StringBuilder successMsg = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();
            result = process.waitFor();
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String s;
                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (successResult != null) {
                    successResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return new CommandResult(result, successMsg == null ? null : successMsg.toString());
    }


    /**
     * Shell命令执行结果类。
     * <p>
     * result为命令的退出码，0表示正常执行，其他值表示错误。
     * successMsg为命令的标准输出内容。
     *
     * @author Trinea
     */
    public static class CommandResult {


        /** 命令退出码，0为正常 */
        public int result;
        /** 命令的标准输出信息 */
        public String successMsg;



        /**
         * 仅设置退出码的构造方法。
         *
         * @param result 命令退出码
         */
        public CommandResult(int result) {
            this.result = result;
        }


        /**
         * 设置退出码和输出信息的构造方法。
         *
         * @param result     命令退出码
         * @param successMsg 命令标准输出
         */
        public CommandResult(int result, String successMsg) {
            this.result = result;
            this.successMsg = successMsg;
        }
    }
}
