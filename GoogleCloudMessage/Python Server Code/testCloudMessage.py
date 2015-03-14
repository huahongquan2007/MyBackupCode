import datetime
import requests
import bs4
import re
from gcm import *
import time

gcm = GCM("AIzaSyBX0PY_o0Om1-sJ2ip_trZ4i-oWPhVWgGo")
reg_id = 'APA91bHJJMMe85saOIeYvQbi0-M5qpvZHP2oW5WrTXRPSLeP6SZZVmwk2h_ntLkI3MNDfsv5cYlyLddbKuvapLjyjKBvCedInyL2J_maHy-Ac7VbSa4QvSFZZyWcNlCeTdRsszZtW_Kvd3QMGEdqS3v40MKaG4UdxRp52pdBVjeES2Vn0Q4Yb7Q'

def checkOnline():

	curTime = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
	print 'Begin check at ' + curTime
	response = requests.get('https://www.kickstarter.com/projects/597507018/pebble-time-awesome-smartwatch-no-compromises/')
	soup = bs4.BeautifulSoup(response.text)
	name = soup.select('.num-backers')
	accountName = name[0].get_text().strip().split(' ')

	if( accountName[0] == '10,000' ):
		print 'Current: ' + accountName[0]
	else:
		data = {'message': accountName[0], 'timestamp': curTime}
		gcm.plaintext_request(registration_id=reg_id, data=data)
		print 'Olala: ' + accountName[0]
	print '/'

	time.sleep(30)

while True:
	checkOnline()

