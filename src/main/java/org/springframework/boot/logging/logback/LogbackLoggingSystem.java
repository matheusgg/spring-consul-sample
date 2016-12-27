package org.springframework.boot.logging.logback;

import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.impl.StaticLoggerBinder;
import org.springframework.boot.logging.LogFile;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.logging.Slf4JLoggingSystem;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.Status;

public class LogbackLoggingSystem extends Slf4JLoggingSystem {

	private static final String CONFIGURATION_FILE_PROPERTY = "logback.configurationFile";

	private static final Map<LogLevel, Level> LEVELS;

	static {
		final Map<LogLevel, Level> levels = new HashMap<>();
		levels.put(LogLevel.TRACE, Level.TRACE);
		levels.put(LogLevel.DEBUG, Level.DEBUG);
		levels.put(LogLevel.INFO, Level.INFO);
		levels.put(LogLevel.WARN, Level.WARN);
		levels.put(LogLevel.ERROR, Level.ERROR);
		levels.put(LogLevel.FATAL, Level.ERROR);
		levels.put(LogLevel.OFF, Level.OFF);
		LEVELS = Collections.unmodifiableMap(levels);
	}

	private static final TurboFilter FILTER = new TurboFilter() {

		@Override
		public FilterReply decide(final Marker marker, final ch.qos.logback.classic.Logger logger, final Level level, final String format, final Object[] params, final Throwable t) {
			return FilterReply.DENY;
		}

	};

	public LogbackLoggingSystem(final ClassLoader classLoader) {
		super(classLoader);
	}

	@Override
	protected String[] getStandardConfigLocations() {
		return new String[]{"logback-test.groovy", "logback-test.xml", "logback.groovy", "logback.xml"};
	}

	@Override
	public void beforeInitialize() {
		final LoggerContext loggerContext = getLoggerContext();
		if (isAlreadyInitialized(loggerContext)) {
			return;
		}
		super.beforeInitialize();
		loggerContext.getTurboFilterList().add(FILTER);
		configureJBossLoggingToUseSlf4j();
	}

	@Override
	public void initialize(final LoggingInitializationContext initializationContext, final String configLocation, final LogFile logFile) {
		final LoggerContext loggerContext = getLoggerContext();
		if (isAlreadyInitialized(loggerContext)) {
			return;
		}
		loggerContext.getTurboFilterList().remove(FILTER);
		super.initialize(initializationContext, configLocation, logFile);
		markAsInitialized(loggerContext);
		if (StringUtils.hasText(System.getProperty(CONFIGURATION_FILE_PROPERTY))) {
			getLogger(LogbackLoggingSystem.class.getName()).warn("Ignoring '" + CONFIGURATION_FILE_PROPERTY + "' system property. Please use 'logging.config' instead.");
		}
	}

	@Override
	protected void loadDefaults(final LoggingInitializationContext initializationContext, final LogFile logFile) {
		final LoggerContext context = getLoggerContext();
		stopAndReset(context);
		final LogbackConfigurator configurator = new LogbackConfigurator(context);
		context.putProperty("LOG_LEVEL_PATTERN", initializationContext.getEnvironment().resolvePlaceholders("${logging.pattern.level:${LOG_LEVEL_PATTERN:%5p}}"));
		new DefaultLogbackConfiguration(initializationContext, logFile).apply(configurator);
		context.setPackagingDataEnabled(true);
	}

	@Override
	protected void loadConfiguration(final LoggingInitializationContext initializationContext, final String location, final LogFile logFile) {
		super.loadConfiguration(initializationContext, location, logFile);
		final LoggerContext loggerContext = getLoggerContext();
		stopAndReset(loggerContext);
		try {
			configureByResourceUrl(initializationContext, loggerContext, ResourceUtils.getURL(location));
		} catch (final Exception ex) {
			throw new IllegalStateException("Could not initialize Logback logging from " + location, ex);
		}
		final List<Status> statuses = loggerContext.getStatusManager().getCopyOfStatusList();
		final StringBuilder errors = new StringBuilder();
		for (final Status status : statuses) {
			if (status.getLevel() == Status.ERROR) {
				errors.append(errors.length() > 0 ? String.format("%n") : "");
				errors.append(status.toString());
			}
		}
		if (errors.length() > 0) {
			throw new IllegalStateException(String.format("Logback configuration error detected: %n%s", errors));
		}
	}

	private void configureByResourceUrl(final LoggingInitializationContext initializationContext, final LoggerContext loggerContext, final URL url) throws JoranException {

		final String plainURL = this.extractPlainURL(url);

		if (plainURL.endsWith("xml")) {
			final JoranConfigurator configurator = new SpringBootJoranConfigurator(initializationContext);
			configurator.setContext(loggerContext);
			configurator.doConfigure(url);
		} else {
			new ContextInitializer(loggerContext).configureByResource(url);
		}
	}

	private String extractPlainURL(final URL url) {
		String completeURL = url.toString();
		if (completeURL.startsWith("http")) {
			completeURL = completeURL.split("\\?")[0];
		}
		return completeURL;
	}

	private void stopAndReset(final LoggerContext loggerContext) {
		loggerContext.stop();
		loggerContext.reset();
		if (isBridgeHandlerAvailable()) {
			addLevelChangePropagator(loggerContext);
		}
	}

	private void addLevelChangePropagator(final LoggerContext loggerContext) {
		final LevelChangePropagator levelChangePropagator = new LevelChangePropagator();
		levelChangePropagator.setResetJUL(true);
		levelChangePropagator.setContext(loggerContext);
		loggerContext.addListener(levelChangePropagator);
	}

	@Override
	public void cleanUp() {
		markAsUninitialized(getLoggerContext());
		super.cleanUp();
		getLoggerContext().getStatusManager().clear();
	}

	@Override
	protected void reinitialize(final LoggingInitializationContext initializationContext) {
		getLoggerContext().reset();
		getLoggerContext().getStatusManager().clear();
		loadConfiguration(initializationContext, getSelfInitializationConfig(), null);
	}

	private void configureJBossLoggingToUseSlf4j() {
		System.setProperty("org.jboss.logging.provider", "slf4j");
	}

	@Override
	public void setLogLevel(final String loggerName, final LogLevel level) {
		getLogger(loggerName).setLevel(LEVELS.get(level));
	}

	@Override
	public Runnable getShutdownHandler() {
		return new ShutdownHandler();
	}

	private ch.qos.logback.classic.Logger getLogger(final String name) {
		final LoggerContext factory = getLoggerContext();
		return factory.getLogger(StringUtils.isEmpty(name) ? Logger.ROOT_LOGGER_NAME : name);

	}

	private LoggerContext getLoggerContext() {
		final ILoggerFactory factory = StaticLoggerBinder.getSingleton().getLoggerFactory();
		Assert.isInstanceOf(LoggerContext.class, factory,
				String.format(
						"LoggerFactory is not a Logback LoggerContext but Logback is on "
								+ "the classpath. Either remove Logback or the competing "
								+ "implementation (%s loaded from %s). If you are using "
								+ "WebLogic you will need to add 'org.slf4j' to "
								+ "prefer-application-packages in WEB-INF/weblogic.xml",
						factory.getClass(), getLocation(factory)));
		return (LoggerContext) factory;
	}

	private Object getLocation(final ILoggerFactory factory) {
		try {
			final ProtectionDomain protectionDomain = factory.getClass().getProtectionDomain();
			final CodeSource codeSource = protectionDomain.getCodeSource();
			if (codeSource != null) {
				return codeSource.getLocation();
			}
		} catch (final SecurityException ex) {
			// Unable to determine location
		}
		return "unknown location";
	}

	private boolean isAlreadyInitialized(final LoggerContext loggerContext) {
		return loggerContext.getObject(LoggingSystem.class.getName()) != null;
	}

	private void markAsInitialized(final LoggerContext loggerContext) {
		loggerContext.putObject(LoggingSystem.class.getName(), new Object());
	}

	private void markAsUninitialized(final LoggerContext loggerContext) {
		loggerContext.removeObject(LoggingSystem.class.getName());
	}

	private final class ShutdownHandler implements Runnable {

		@Override
		public void run() {
			getLoggerContext().stop();
		}

	}

}
