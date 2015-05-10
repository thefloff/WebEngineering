package controllers;

import at.ac.tuwien.big.we15.lab2.api.Avatar;
import at.ac.tuwien.big.we15.lab2.api.JeopardyFactory;
import at.ac.tuwien.big.we15.lab2.api.JeopardyGame;
import at.ac.tuwien.big.we15.lab2.api.QuestionDataProvider;
import at.ac.tuwien.big.we15.lab2.api.impl.JSONQuestionDataProvider;
import at.ac.tuwien.big.we15.lab2.api.impl.PlayJeopardyFactory;
import model.UserJPA;
import play.data.validation.Constraints;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.*;
import play.data.Form;
import views.html.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

import play.Logger;

import javax.persistence.Query;

import static play.data.Form.form;
import static play.data.validation.Constraints.Required;

public class Application extends Controller {

    private static JeopardyFactory factory = new PlayJeopardyFactory("data.de.json");
    private static QuestionDataProvider questionDataProvider;
    private static Map<String, JeopardyGame> games = new HashMap<>();

    static {
        InputStream is = null;
        try {
            is = new FileInputStream("conf/data.de.json");
        } catch (FileNotFoundException e) {
            Logger.error("No Question Data found \n" + e.getStackTrace());
        }
        questionDataProvider = new JSONQuestionDataProvider(is, factory);
    }

    //show login page
    public static Result index() {
        return ok(authentication.render());
    }

    //on login attempt in login-page
    @Transactional
    public static Result login() {
        Form<UserLogin> loginForm = form(UserLogin.class).bindFromRequest();
        String username = loginForm.get().username;
        String password = loginForm.get().password;

        Logger.debug("Login:\n" +
                "username: "+ username + "\n" +
                "password: " + password);

        Query q = JPA.em().createQuery("select p from UserJPA p where name = :name");
        q.setParameter("name", username);
        List<UserJPA> users = q.getResultList();
        if(users.size() != 1) {
             Logger.debug("User " + username + " not found!");
             return badRequest(authentication.render());
        }
        if(!users.get(0).getPassword().equals(password)) {
            Logger.debug("Incorrect password!");
            return badRequest(authentication.render());
        }


        JeopardyGame game = factory.createGame(users.get(0));
        games.put(username, game);
        session("connected", username);
        return ok(jeopardy.render(game));
    }

    //on navigation to register from login-page
    public static Result goToRegister() {
        UserRegister defaultValues = new UserRegister();
        defaultValues.firstname = "Vorname2";
        defaultValues.lastname = "test";
        defaultValues.gender = "test";
        defaultValues.avatar = "test";
        defaultValues.birthdate = new Date();
        defaultValues.username = "test";
        defaultValues.password = "test";
        Form<UserRegister> form = form(UserRegister.class);//.fill(defaultValues);

        return ok(registration.render(form));
    }

    //on registration attempt on registration-page
    @Transactional
    public static Result register() {
        Form<UserRegister> registrationForm = form(UserRegister.class).bindFromRequest();

        if(registrationForm.hasErrors()) {
            registrationForm.reject("Errors");
            return badRequest(registration.render(registrationForm));
        } else {
            String firstname = registrationForm.get().firstname;
            String lastname = registrationForm.get().lastname;
            Date birthdate = registrationForm.get().birthdate;
            String gender = registrationForm.get().gender;
            String avatar = registrationForm.get().avatar;
            String username = registrationForm.get().username;
            String password = registrationForm.get().password;

            Logger.debug("Register:\n" +
                    "firstname: " + firstname + "\n" +
                    "lastname: "+ lastname + "\n" +
                    "birthdate: "+ birthdate +"\n" +
                    "gender: "+ gender + "\n"+
                    "avatar: "+ avatar + "\n" +
                    "username: "+ username + "\n" +
                    "password: " + password);


            UserJPA user = new UserJPA();
            user.setFirstname(firstname);
            user.setLastname(lastname);
            user.setBirthdate(birthdate);
            if(gender.equals("male")) {
                user.setMale(true);
            }else {
                user.setMale(false);
            }
            if(avatar != null) {
                user.setAvatar(Avatar.valueOf(avatar.replace("-", "_").toUpperCase()));
            }else {
                // validation error
            }
            user.setName(username);
            user.setPassword(password);

            Query q = JPA.em().createQuery("select p from UserJPA p where name = :name");
            q.setParameter("name", username);
            List<UserJPA> users = q.getResultList();
            if(users.size() > 0) {
                Logger.debug("Username bereits vergeben");
                registrationForm.reject("username", "Username bereits vergeben");
                return badRequest(registration.render(registrationForm));
            }

            if(username == null || username.length() < 4 || username.length() > 8) {
                Logger.debug("Username muss mindestens 4 und höchstens 8 Zeichen lang sein!");
                registrationForm.reject("username", "Username muss mindestens 4 und höchstens 8 Zeichen lang sein!");
                return badRequest(registration.render(registrationForm));
            }
            if(password == null || password.length() < 4 || password.length() > 8) {
                Logger.debug("Passwort muss mindestens 4 und höchstens 8 Zeichen lang sein!");
                registrationForm.reject("password", "Passwort muss mindestens 4 und höchstens 8 Zeichen lang sein!");
                return badRequest(registration.render(registrationForm));
            }

            JPA.em().persist(user);

            return ok(authentication.render());
        }
    }

    //on logout pressed on any page
    public static Result logout() {
        String username = session("connected");
        if(username !=null) {
            games.remove(username);
            session().clear();
        }

        return ok(authentication.render());
    }

    //on question selected on jeopardy-page
    @Security.Authenticated(Authentication.class)
    public static Result questionSelected() {
        Form<QuestionSelection> questionSelectionForm = form(QuestionSelection.class).bindFromRequest();
        int questionNR = questionSelectionForm.get().question_selection;

        JeopardyGame game = games.get(session().get("connected"));
        game.chooseHumanQuestion(questionNR);

        // TODO: set question for next page

        return ok(question.render(game));
    }

    @Security.Authenticated(Authentication.class)
    public static Result answersSelected() {
        Form<AnswerSelection> answerSelectionForm = form(AnswerSelection.class).bindFromRequest();
        List<Integer> answers = answerSelectionForm.get().answers;

        JeopardyGame game = games.get(session().get("connected"));
        game.answerHumanQuestion(answers);
        if(game.isGameOver()) {
            return ok(winner.render(game));
        }
        return ok(jeopardy.render(game));
    }

    @Security.Authenticated(Authentication.class)
    public static Result newGame() {
        String username = session("connected");
        JeopardyGame oldGame = games.get(username);
        JeopardyGame game = factory.createGame(oldGame.getHuman());
        games.put(username, game);

        return ok(jeopardy.render(game));
    }

    @Security.Authenticated(Authentication.class)
    public static Result winner() {
        JeopardyGame game = games.get(session().get("connected"));
        return ok(winner.render(game));
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
        @Required
        public String username;
        @Required
        public String password;

        public String validate() {
            if(firstname.equals("floff")) {
                return "Name already taken!";
            }
            return null;
        }
    }

    public static class QuestionSelection {
        public int question_selection;
    }

    public static class AnswerSelection {
        public List<Integer> answers;
    }

}



