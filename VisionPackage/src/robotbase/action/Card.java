package robotbase.action;

public class Card {
	 
	
	public static String getBlueMessageCard(String icon, String message){
			
		String html = "<div class=\"panel panel-default\"><div class=\"panel-body blue_card\">"
						+ message + "</div></div>";	
		return html;
		
		
	}
	
	public static String getRedMessageCard(String icon, String message){
		
		String html = "<div class=\"panel panel-default\"><div class=\"panel-body red_card\">"
						+ message + "</div></div>";	
		return html;
		
		
	}
	
	public static String getNumberCard(String card_style, String bigText, String smallText){
		
		String html = "<div class=\"panel panel-default\">"
					   +"<div class=\"panel-body "+card_style+"\">"
					   +"<h1 class=\"big_text\">"+bigText+"</h1>"
					   +"<p>"+smallText+"</p> "
					   +"</div>"
					  +"</div>";
		return html;
	}

	public static String getWhiteCard(String string) {
		// TODO Auto-generated method stub
		String html = "<div class=\"panel panel-default\"><div class=\"panel-body white_card\">"
				+ string + "</div></div>";	
		return html;
	}
	
}
