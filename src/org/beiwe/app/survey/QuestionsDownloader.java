package org.beiwe.app.survey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.beiwe.app.R;
import org.beiwe.app.storage.TextFileManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.util.Log;

public class QuestionsDownloader {

	private Context appContext;
	
	public QuestionsDownloader(Context applicationContext) {
		this.appContext = applicationContext; 
	}
	
	
	public void downloadJsonQuestions() {
		new GetUpToDateSurvey().execute(" ");
	}


	public String getJsonSurveyString() {
		try {
			// Try loading the questions.json file from the local filesystem
			return getSurveyQuestionsFromFilesystem();
		}
		catch (Exception e2) {
			/* If the app hasn't downloaded questions.json and saved it to
			 * the filesystem, return an empty String, which will break the
			 * JSON parser and display an error message instead of the survey */
			return "";
		}
	}
	
	
	/**
	 * Read a file from the server, and return the file as a String 
	 * @throws NotFoundException 
	 * @throws IOException 
	 * @throws JSONException 
	 */
	private String getSurveyQuestionsFromServer() throws NotFoundException, IOException, JSONException {
		Log.i("QuestionsDownloader", "Called getSurveyQuestionsFromServer()");
		
		// Get the URL of the Survey Questions JSON file
		String urlString = appContext.getResources().getString(R.string.survey_questions_url);
		URL questionsFileURL = new URL(urlString);
		
		// Set up an HTTP connection
		HttpURLConnection connection = (HttpURLConnection) (questionsFileURL.openConnection());

		// Throw a TimeoutException after this many milliseconds if
		connection.setConnectTimeout(3000); // The server hasn't accepted the connection
		connection.setReadTimeout(5000); // This device hasn't received the response
		connection.connect();
		
		// Set up a BufferedReader from the HTTP connection
		InputStreamReader inputReader = new InputStreamReader(connection.getInputStream());
		BufferedReader reader = new BufferedReader(inputReader);
		
		// Set up a StringBuilder (uses less memory than repeatedly appending to a String)
		StringBuilder builder = new StringBuilder();
		String aux = "";
		
		// Read in the file from the BufferedReader, and append it to the StringBuilder
		while ((aux = reader.readLine()) != null) {
			builder.append(aux);
		}
		connection.disconnect();
		
		// Save the Survey Questions JSON file to the local filesystem
		String surveyQuestions = builder.toString();
		
		if (isValidJson(surveyQuestions)) {
			return surveyQuestions;
		}
		else {
			throw new JSONException("Invalid JSON");
		}
	}
	
	
	/**
	 * Read a file from the local Android filesystem, and return it as a String
	 * @throws JSONException 
	 */
	private String getSurveyQuestionsFromFilesystem() throws NullPointerException, JSONException {
		Log.i("QuestionsDownloader", "Called getSurveyQuestionsFromFilesystem()");
		
		String surveyQuestions = TextFileManager.getCurrentQuestionsFile().read();

		if (isValidJson(surveyQuestions)) {
			return surveyQuestions;
		}
		else {
			throw new JSONException("Invalid JSON");
		}
	}
	
	
	/**
	 * Write a string (the JSON representation of survey questions) to a file
	 * in the local Android Filesystem
	 * @param string the JSON representation of survey questions
	 */
	private void writeStringToFile(String string) {
		TextFileManager.getCurrentQuestionsFile().deleteSafely();
		TextFileManager.getCurrentQuestionsFile().write(string);
	}

	
	/**
	 * Tells you whether a String is valid JSON
	 * Based on: http://stackoverflow.com/a/10174938
	 * @param
	 * @return true if valid JSON; false otherwise
	 */
	private boolean isValidJson(String input) {
		try {
			new JSONObject(input);
		}
		catch (JSONException e) {
			try {
				new JSONArray(input);
			}
			catch (JSONException e2) {
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Gets the most up-to-date version of the survey; does it on a separate,
	 * non-blocking thread, because it's a slow network request
	 */
	class GetUpToDateSurvey extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			try {
				return getSurveyQuestionsFromServer();
			} catch (Exception e) {
				Log.i("QUESTIONSDOWNLOADER", "getSurveyQuestionsFromServer() failed with exception " + e);
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null) {
				writeStringToFile(result);
			}
		}
	}

}
