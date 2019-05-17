package util;

public class SimpleLogger {
    public enum LogType{
        INFO("[+]"),
        ERROR("[!]"),
        DEBUG("[>]");

        public final String label;

        private LogType(String label) {
            this.label = label;
        }
        public String toString(){
            return label;
        }
    }

    public static void log(LogType logType, String stuff){
        System.out.println(logType + " " + stuff);
    }
}
