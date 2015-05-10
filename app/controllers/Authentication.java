package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.*;

import models.*;

public class Authentication extends Security.Authenticator {

    @Override
    public String getUsername(Context ctx) {
        return ctx.session().get("connected");
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(controllers.routes.Application.index());
    }
}