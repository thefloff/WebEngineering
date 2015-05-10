package controllers;

import play.mvc.*;
import play.data.Form;
import views.html.*;

import java.util.Date;
import java.util.List;
import java.util.Set;
import play.Logger;

import static play.data.Form.form;

public class Application extends Controller {

    //show login page
    public static Result index() {
        return ok(authentication.render());
    }

    //on login attempt in login-page
    public static Result login() {
        Form<UserLogin> loginForm = form(UserLogin.class).bindFromRequest();
        String username = loginForm.get().username;
        String password = loginForm.get().password;

        // TODO: check if correct, create game etc

        return ok(jeopardy.render());
    }

    //on navigation to register from login-page
    public static Result goToRegister() {
        return ok(registration.render());
    }

    //on registration attempt on registration-page
    public static Result register() {
        Form<UserRegister> registrationForm = form(UserRegister.class).bindFromRequest();
        String firstname = registrationForm.get().firstname;
        String lastname = registrationForm.get().lastname;
        Date birthdate = registrationForm.get().birthdate;
        String gender = registrationForm.get().gender;
        String avatar = registrationForm.get().avatar;
        String username = registrationForm.get().username;
        String password = registrationForm.get().password;

        Logger.debug("firstname: " + firstname + "\n" +
                "lastname: "+ lastname + "\n" +
                "birthdate: "+ birthdate +"\n" +
                "gender: "+ gender + "\n"+
                "avatar: "+ avatar + "\n" +
                "username: "+ username + "\n" +
                "password: " + password);

        // TODO: check if ok, create user in DB etc

        return ok(authentication.render());
    }

    //on logout pressed on any page
    public static Result logout() {

        // TODO: end game etc

        return ok(authentication.render());
    }

    //on question selected on jeopardy-page
    public static Result questionSelected() {
        Form<QuestionSelection> questionSelectionForm = form(QuestionSelection.class).bindFromRequest();
        int questionNR = questionSelectionForm.get().question_selection;

        // TODO: set question for next page

        return ok(question.render());
    }

    public static Result answersSelected() {
        Form<AnswerSelection> answerSelectionForm = form(AnswerSelection.class).bindFromRequest();
        List<Integer> answers = answerSelectionForm.get().answers;
        
        return ok(jeopardy.render());
    }

    public static Result newGame() {
        return ok(jeopardy.render());
    }

    public static Result winner() {
        return ok(winner.render());
    }

    // TODO: MISSING:   question -> jeopardy
    // TODO: MISSING:   everything with winner





    //classes for form-submission
    public static class UserLogin {
        public String username;
        public String password;
    }

    public static class UserRegister {
        public String firstname;
        public String lastname;
        public Date birthdate;
        public String gender;
        public String avatar;
        public String username;
        public String password;
    }

    public static class QuestionSelection {
        public int question_selection;
    }

    public static class AnswerSelection {
        public List<Integer> answers;
    }

}



