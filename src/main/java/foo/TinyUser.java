package foo;


public class TinyUser {
	private String email;
	private String name;
	private String url;
	
	public TinyUser() {
		
	}
	
	public String getEmail() {
		return this.email;
	}

	public String getName() {
		return this.name;
	}

	public String getUrl() {
		return this.url;
	}
	
	public void setName(String n) {
		this.name=n;
	}

	public void setEmail(String em) {
		this.email=em;
	}
	
	public void setUrl(String url) {
		this.url=url;
	}
}
