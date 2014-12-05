package robotbase.abilities.home_security;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class FaceRecognitionAlarm extends Service{
	// Robot thay 1 khuon mat:
	// - Known Face: Says "Hello", "Good morning" ... 1 khoang thoi gian 1 lan. Vi du: Neu thoi gian cach lan truoc > 1h thi moi chao lai. Hoac neu biet nguoi do da ra khoi nha thi moi chao lai
	// - Unknown face: Say: "Who are you?" ---> add vao dataset: UnknownDb. Chi noi who are you 1 lan voi 1 nguoi trong 1 khoang thoi gian 
	// - De them 1 nguoi: Can phai la 1 known face ra lenh "remember me" thi moi them nguoi moi. Tranh truong hop trom them nguoi
	// - Sau khi them nguoi vao database thi can remove ra khoi UnknownDB
	@Override
	public IBinder onBind(Intent intent) {
		
		return null;
	}

}
