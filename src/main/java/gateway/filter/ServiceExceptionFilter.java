package gateway.filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpStatus;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import gateway.logger.Logger;

public class ServiceExceptionFilter extends ZuulFilter {
	private static final String STACKTRACE = "trace";

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public String filterType() {
		return FilterConstants.POST_TYPE;
	}

	@Override
	public int filterOrder() {
		return -1;
	}

	/**
	 * Filter nachdem der Service die Anfrage bearbeitet hat. 
	 * Filtert nach einen Status 500 Fehler und loggt den Fehler.
	 * Der StackTrace wird aus dem Json entnommen und geloggt. Der Benutzer erhält keinen StackTrace in der Response.
	 */
	@Override
	public Object run() throws ZuulException {
		RequestContext ctx = RequestContext.getCurrentContext();
		if (is500ServerError(ctx)) {
			if(isZuulError(ctx)) {
				Logger.error(ctx.getThrowable().getCause() != null ? 
						ctx.getThrowable().getCause() : ctx.getThrowable());
			}else if(isClientError(ctx)) {
				handleClientError(ctx);
			}
		}
		return null;
	}

	private static void handleClientError(RequestContext ctx) {
		try (final InputStream responseDataStream = ctx.getResponseDataStream()) {
			JsonObject json = getJsonFromInputStream(responseDataStream);
			
			if (json.get(STACKTRACE) != null) {
				//Server Error werden geloggt
				if (is500ServerError(ctx)) {
					String exception = json.get(STACKTRACE).getAsString().split("\\r\\n")[0];
					String applicationName = ctx.getRequest().getRequestURI().split("/")[1].toUpperCase();
					Logger.error(applicationName + " - Fehler: " + exception, json.get(STACKTRACE).getAsString());
				}
				json.remove(STACKTRACE);
			}
			ctx.setResponseBody(json.toString());
		} catch (Exception e) {
			Logger.error(e);	
		}
		
	}
	
	/**
	 * Wenn die Response ein Throwable enthält kommt der Fehler aus vom Zuul-Gateway. Andernfalls kommen die Fehler aus den Services.
	 */
	private static boolean isZuulError(RequestContext ctx) {
		return ctx.getThrowable() != null && HttpStatus.valueOf(ctx.getResponse().getStatus()).is5xxServerError();
	}
	
	/**
	 * Wenn der ResponseDataStream ungleich null ist, handelt es sich um einen Fehler im Client.
	 */
	private static boolean isClientError(RequestContext ctx) {
		return ctx.getResponseDataStream() != null;
	}
	
	/**
	 * Prüft, ob es sich um einen intenren Servererror handelt mit HttpStatus 500.
	 */
	private static boolean is500ServerError(RequestContext ctx) {
		return HttpStatus.valueOf(ctx.getResponse().getStatus()).is5xxServerError();
	}
	
	private static JsonObject getJsonFromInputStream(InputStream responseDataStream) throws IOException {
		String jsonStr = CharStreams.toString(new InputStreamReader(responseDataStream, StandardCharsets.UTF_8));
		return new Gson().fromJson(jsonStr, JsonObject.class);
	}
}
