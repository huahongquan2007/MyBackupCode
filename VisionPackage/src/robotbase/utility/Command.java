package robotbase.utility;

import java.util.List;
import org.json.JSONArray;
 
public class Command {
	
	private String id;
	private List<Provider> providers ; 
	private List<String> subcommands ;
	private org.json.JSONArray params ; 
	
	private String text;
	private String default_text;
	private boolean editable;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<Provider> getProviders() {
		return providers;
	}
	public void setProviders(List<Provider> providers) {
		this.providers = providers;
	}
	public List<String> getSubcommands() {
		return subcommands;
	}
	public void setSubcommands(List<String> subcommands) {
		this.subcommands = subcommands;
	}
	public JSONArray getParams() {
		return params;
	}
	public void setParams(org.json.JSONArray params2) {
		this.params = params2;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getDefault_text() {
		return default_text;
	}
	public void setDefault_text(String default_text) {
		this.default_text = default_text;
	}
	public boolean getEditable() {
		return editable;
	}
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	@Override
	public String toString() {
		return "Command [id=" + id
				+ ", text=" + text + ", default_text=" + default_text
				+ ", editable=" + editable + "]";
	}
	/**
	 * @param id
	 * @param providers
	 * @param subcommands
	 * @param params
	 * @param text
	 * @param default_text
	 * @param editable
	 */
	public Command(String id, List<Provider> providers,
			List<String> subcommands, JSONArray params,
			String text, String default_text, String editable) {
		super();
		this.id = id;
		this.providers = providers;
		this.subcommands = subcommands;
		this.params = params;
		this.text = text;
		this.default_text = default_text;
		this.editable = Boolean.parseBoolean(editable);
	}
	public Command() {
		// TODO Auto-generated constructor stub
	}
	
	public Command findInList(List<Command> list , String id) {
		
		for(int i =0; i< list.size(); i++){
			if( id.equalsIgnoreCase(list.get(i).getId()) ==  true ){
				return list.get(i);
			}
		}
		
		return null;
		
	}
	
}
