echo show
netsh wlan set hostednetwork mode=allow ssid=authomobile_001 key=899088806770122078805660
netsh wlan start hostednetwork
pause
netsh wlan stop hostednetwork