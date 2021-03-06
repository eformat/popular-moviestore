package org.acme.mvc;

import io.vertx.core.Vertx;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Singleton
public class Thymeleaf {

    @Inject
    private Vertx vertx;

    private ThymeleafTemplateEngine engine;

    @PostConstruct
    public void setup() {
        engine = ThymeleafTemplateEngine.create(vertx);
        configureThymeleafEngine(engine);
    }

    private void configureThymeleafEngine(ThymeleafTemplateEngine engine) {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        engine.getThymeleafTemplateEngine().setTemplateResolver(templateResolver);
    }

    public Rendering view(String template) {
        return Rendering.of(engine).view(template);
    }

    public static class Rendering {

        private TemplateEngine engine;
        private String view;
        private Map<String, Object> attributes;

        private Rendering(TemplateEngine engine, String view, Map<String, Object> attributes) {
            this.engine = engine;
            this.view = view;
            this.attributes = attributes;
        }

        private static Rendering of(TemplateEngine engine) {
            Objects.requireNonNull(engine, "Template engine may not be null.");
            return new Rendering(engine, "home.html", Collections.emptyMap());
        }

        private Rendering view(String view) {
            Objects.requireNonNull(view, "Template view may not be null.");
            this.view = view;
            return this;
        }

        public CompletableFuture<Response> render(Map<String, Object> attributes) {
            Objects.requireNonNull(attributes, "Template attributes may not be null.");
            this.attributes = attributes;
            return render();
        }

        public CompletableFuture<Response> render() {
            CompletableFuture<Response> response = new CompletableFuture<>();

            engine.render(this.attributes, "/" + this.view, r -> {
                // response.completeExceptionally(new RuntimeException("oops"));
                if (r.failed()) response.complete(
                        Response.status(Response.Status.BAD_REQUEST)
                                .entity(r.cause().getLocalizedMessage())
                                .type(MediaType.APPLICATION_JSON)
                                .build());
                else response.complete(
                        Response.ok()
                                .entity(r.result().toString(StandardCharsets.UTF_8.displayName()))
                                .type(MediaType.TEXT_HTML)
                                .build());
            });
            return response;
        }
    }
}
