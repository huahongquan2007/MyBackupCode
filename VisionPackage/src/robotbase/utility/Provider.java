package robotbase.utility;

 
public class Provider {

	private String id;
	private String name; 
	private String key ;
	private String logo ;
	private String linked_logo; 
	private String website ;
	private String description; 
	private String term ;
	private String permission_required; 
	private String status ;
	private String note ;
	private String created_at; 
	private String updated_at ;
	private String absolute_logo; 
	
	public Provider(){
		this.name = "";
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public String getLinked_logo() {
		return linked_logo;
	}
	public void setLinked_logo(String linked_logo) {
		this.linked_logo = linked_logo;
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public String getPermission_required() {
		return permission_required;
	}
	public void setPermission_required(String permission_required) {
		this.permission_required = permission_required;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}
	public String getUpdated_at() {
		return updated_at;
	}
	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}
	public String getAbsolute_logo() {
		return absolute_logo;
	}
	public void setAbsolute_logo(String absolute_logo) {
		this.absolute_logo = absolute_logo;
	}
	
	public Provider(String id, String name, String key, String logo,
			String linked_logo, String website, String description,
			String term, String permission_required, String status,
			String note, String created_at, String updated_at,
			String absolute_logo) {
		super();
		this.id = id;
		this.name = name;
		this.key = key;
		this.logo = logo;
		this.linked_logo = linked_logo;
		this.website = website;
		this.description = description;
		this.term = term;
		this.permission_required = permission_required;
		this.status = status;
		this.note = note;
		this.created_at = created_at;
		this.updated_at = updated_at;
		this.absolute_logo = absolute_logo;
	}
	
	@Override
	public String toString() {
		return "Provider [id=" + id + ", name=" + name + ", key=" + key
				+ ", logo=" + logo + ", linked_logo=" + linked_logo
				+ ", website=" + website + ", description=" + description
				+ ", term=" + term + ", permission_required="
				+ permission_required + ", status=" + status + ", note=" + note
				+ ", created_at=" + created_at + ", updated_at=" + updated_at
				+ ", absolute_logo=" + absolute_logo + "]";
	}
	

}
