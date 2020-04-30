package gateway.logger;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import org.slf4j.LoggerFactory;

public class Logger {
	public static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Logger.class);
	
	private static final DateTimeFormatter DTF = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
	
	private Logger() {}
	
	public static void error(final Throwable ex) {
		String logText = buildLogText();
		LOG.error(logText, ex);
	}
	
	public static void error(final String subject, final String stackTrace) {
		String logText = buildLogText();
		LOG.error("{}{}{}", subject, logText, stackTrace);
	}
	
	private static String buildLogText() {
		String now = LocalDateTime.now().format(DTF);
		
		Instant instant = Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime());
	    String runtime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime().format(DTF);
		
		StringBuilder builder = new StringBuilder();
		builder.append("\r\n");
		
		builder.append("Datum: ").append(now).append("\r\n");
		builder.append("Startzeit: ").append(runtime).append("\r\n");
		builder.append("Benutzer: ").append(System.getProperty("user.name")).append("\r\n\r\n");
		builder.append("\r\n\r\n");
		return builder.toString();
	}
}
