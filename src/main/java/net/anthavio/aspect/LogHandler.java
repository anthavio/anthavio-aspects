package net.anthavio.aspect;

import net.anthavio.aspect.LogAspect.ExecStats;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.slf4j.Logger;

/**
 * 
 * @author martin.vanek
 *
 */
public interface LogHandler {

	public void beforeExecution(ProceedingJoinPoint pjp, Logged cfg);

	public void afterExecution(ProceedingJoinPoint pjp, Logged cfg, long startMillis);

	public void whenException(Exception x, long startMillis);

	public static class DefaultLogHandler implements LogHandler {

		@Override
		public void beforeExecution(ProceedingJoinPoint pjp, Logged cfg) {
			long startMillis = System.currentTimeMillis();
			Signature signature = pjp.getSignature();
			final Logger logger = getLogger(signature);

			//parameter values and return values are logged only on debug/trace level or when forced
			boolean logValues = logger.isDebugEnabled() || logger.isTraceEnabled() || cfg.forceValues();

			if (cfg.mode() == Logged.Mode.AROUND || cfg.mode() == Logged.Mode.ENTER) {
				print(buildEnterMessage(pjp, cfg, logValues), logger);
			}

		}

		@Override
		public void afterExecution(ProceedingJoinPoint pjp, Logged cfg, Object retVal, long startMillis) {
			if (cfg.mode() == Logged.Mode.AROUND || cfg.mode() == Logged.Mode.EXIT) {
				long execMillis = System.currentTimeMillis() - startMillis;
				String message = buildExitMessage(signature, cfg, logValues, execMillis, retVal);
				print(message, logger);
				if (cfg.statistics()) {
					ExecStats stats = statsMap.get(signature);
					if (stats == null) {
						stats = new ExecStats();
						statsMap.put(signature, stats);
					}
					stats.execution(startMillis, execMillis);
				}
			}

		}

		@Override
		public void whenException(Exception x, long startMillis) {
			long execMillis = System.currentTimeMillis() - startMillis;
			printException(signature, cfg, logger, execMillis, x);
			if (cfg.statistics()) {
				ExecStats stats = statsMap.get(signature);
				if (stats == null) {
					stats = new ExecStats();
					statsMap.put(signature, stats);
				}
				stats.exception(startMillis, execMillis);
			}
			throw x;

		}

	}
}
