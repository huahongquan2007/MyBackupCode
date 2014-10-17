package robotbase.abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NLPModel {

	public NLPModel() {

	}

	public NLPModel(String nlp_data) {
		parse(nlp_data);
	}

	public class NLP {
		public String module;
		public JSONObject params;
		public Expression expression = new Expression();
		public Provider provider = new Provider();
		public UserApp user_app = new UserApp();

		public class Expression {
			public String api_method;
			public String api_url;
			public String command;
			public int id;
			public List<String> keywords = new ArrayList<String>();
			public String module;
			public String name;
			public String note;
			public String provider_name;
			public boolean status;

			public boolean hasKeyword(String find_string) {
				for (String keyword : keywords) {
					if (keyword.indexOf(find_string) > -1) {
						return true;
					}
				}
				return false;
			}
		}

		public class Provider {
			public String access_token_url;
			public String auth_type;
			public String authorize_url;
			public boolean auto_provider;
			public String callback_url;
			public String client_id;
			public String client_secret;
			public String consumer_key;
			public String consumer_secret;
			public int developer_id;
			public String device_type;
			public int id;
			public boolean is_local;
			public String key;
			public String linked_logo;
			public String list_device_api_data;
			public String logo;
			public String name;
			public String note;
			public String password;
			public int permission_required;
			public String request_token_url;
			public String scopes;
			public String signature_type;
			public int sort;
			public boolean status;
			public String term;
			public String user_info_api_data;
			public String username;
			public String website;
		}

		public class UserApp {
			public String data;
			public String email;
			public String expires_at;
			public String icon;
			public int id;
			public int is_viewed;
			public String note;
			public String provider_name;
			public String refresh_token;
			public String secret;
			public int sort;
			public boolean status;
			public String token;
			public String user_app_id;
			public String user_id;
			public String user_name;
		}
	}

	public List<String> hypothesis = new ArrayList<String>();
	public NLP nlp = new NLP();
	public boolean success = false;

	public void parse(String nlp_data) {
		try {
			JSONObject nlp_json = new JSONObject(nlp_data);
			success = nlp_json.getBoolean("success");
			if (success) {
				JSONArray hypothesis = nlp_json.getJSONArray("hypothesis");
				for (int i = 0; i < hypothesis.length(); i++) {
					this.hypothesis.add(hypothesis.getString(i));
				}

				JSONObject nlp = nlp_json.optJSONObject("nlp");

				if (nlp == null) {
					success = false;
					return;
				}

				this.nlp.module = nlp.getString("module");

				// expression
				JSONObject expression = nlp.getJSONObject("expression");
				this.nlp.expression.api_method = expression
						.getString("api_method");
				this.nlp.expression.api_url = expression.getString("api_url");
				this.nlp.expression.command = expression.getString("command");
				this.nlp.expression.id = expression.getInt("id");
				JSONArray keywords = new JSONArray(
						expression.getString("keywords"));
				for (int i = 0; i < keywords.length(); i++) {
					this.nlp.expression.keywords.add(keywords.getString(i));
				}
				this.nlp.expression.module = expression.getString("module");
				this.nlp.expression.name = expression.getString("name");
				this.nlp.expression.note = expression.getString("note");
				this.nlp.expression.provider_name = expression
						.getString("provider_name");
				this.nlp.expression.status = expression.getBoolean("status");

				// params
				this.nlp.params = nlp.getJSONObject("params");

				// provider
				JSONObject provider = nlp.getJSONObject("provider");
				if (provider.has("access_token_url")) {
					this.nlp.provider.access_token_url = provider
							.getString("access_token_url");
				}
				if (provider.has("auth_type")) {
					this.nlp.provider.auth_type = provider
							.getString("auth_type");
				}
				if (provider.has("authorize_url")) {
					this.nlp.provider.authorize_url = provider
							.getString("authorize_url");
				}
				if (provider.has("auto_provider")) {
					this.nlp.provider.auto_provider = provider
							.getBoolean("auto_provider");
				}
				if (provider.has("callback_url")) {
					this.nlp.provider.callback_url = provider
							.getString("callback_url");
				}
				if (provider.has("client_id")) {
					this.nlp.provider.client_id = provider
							.getString("client_id");
				}
				if (provider.has("client_secret")) {
					this.nlp.provider.client_secret = provider
							.getString("client_secret");
				}
				if (provider.has("consumer_key")) {
					this.nlp.provider.consumer_key = provider
							.getString("consumer_key");
				}
				if (provider.has("consumer_secret")) {
					this.nlp.provider.consumer_secret = provider
							.getString("consumer_secret");
				}
				if (provider.has("developer_id")) {
					this.nlp.provider.developer_id = provider
							.getInt("developer_id");
				}
				if (provider.has("device_type")) {
					this.nlp.provider.device_type = provider
							.getString("device_type");
				}
				if (provider.has("id")) {
					this.nlp.provider.id = provider.getInt("id");
				}
				if (provider.has("is_local")) {
					this.nlp.provider.is_local = provider
							.getBoolean("is_local");
				}
				if (provider.has("key")) {
					this.nlp.provider.key = provider.getString("key");
				}
				if (provider.has("linked_logo")) {
					this.nlp.provider.linked_logo = provider
							.getString("linked_logo");
				}
				if (provider.has("list_device_api_data")) {
					this.nlp.provider.list_device_api_data = provider
							.getString("list_device_api_data");
				}
				if (provider.has("logo")) {
					this.nlp.provider.logo = provider.getString("logo");
				}
				if (provider.has("name")) {
					this.nlp.provider.name = provider.getString("name");
				}
				if (provider.has("note")) {
					this.nlp.provider.note = provider.getString("note");
				}
				if (provider.has("password")) {
					this.nlp.provider.password = provider.getString("password");
				}
				if (provider.has("permission_required")) {
					this.nlp.provider.permission_required = provider
							.getInt("permission_required");
				}
				if (provider.has("request_token_url")) {
					this.nlp.provider.request_token_url = provider
							.getString("request_token_url");
				}
				if (provider.has("scopes")) {
					this.nlp.provider.scopes = provider.getString("scopes");
				}
				if (provider.has("signature_type")) {
					this.nlp.provider.signature_type = provider
							.getString("signature_type");
				}
				if (provider.has("sort")) {
					this.nlp.provider.sort = provider.getInt("sort");
				}
				if (provider.has("status")) {
					this.nlp.provider.status = provider.getBoolean("status");
				}
				if (provider.has("term")) {
					this.nlp.provider.term = provider.getString("term");
				}
				if (provider.has("user_info_api_data")) {
					this.nlp.provider.user_info_api_data = provider
							.getString("user_info_api_data");
				}
				if (provider.has("username")) {
					this.nlp.provider.username = provider.getString("username");
				}
				if (provider.has("website")) {
					this.nlp.provider.website = provider.getString("website");
				}
				
				// UserApp
				JSONObject user_app = nlp.optJSONObject("user_app");
				if(user_app != null) {
					this.nlp.user_app.data = user_app.getString("data");
					this.nlp.user_app.email = user_app.getString("email");
					this.nlp.user_app.expires_at = user_app.getString("expires_at");
					this.nlp.user_app.icon = user_app.getString("icon");
					this.nlp.user_app.id = user_app.getInt("id");
					this.nlp.user_app.is_viewed = user_app.getInt("is_viewed");
					this.nlp.user_app.note = user_app.getString("note");
					this.nlp.user_app.provider_name = user_app.getString("provider_name");
					this.nlp.user_app.refresh_token = user_app.getString("refresh_token");
					this.nlp.user_app.secret = user_app.getString("secret");
					this.nlp.user_app.sort = user_app.getInt("sort");
					this.nlp.user_app.status = user_app.getBoolean("status");
					this.nlp.user_app.token = user_app.getString("token");
					this.nlp.user_app.user_app_id = user_app.getString("user_app_id");
					this.nlp.user_app.user_id = user_app.getString("user_id");
					this.nlp.user_app.user_name = user_app.getString("user_name");
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
