# Astrill-Asus
This loads the Astrill VPN firmware onto the Asus RT-AX88U Pro router.

        This loads the Astrill VPN firmware onto the Asus RT-AX88U Pro router.  It was developed so that the router
        firmware could be easily updated from a mobile phone when a computer is not available.  The Asus router
        must have Asuswrt-Merlin firmware already loaded.
        This has only been tested on the Asus RT-AX88U Pro router.

        The Astrill firmware can be obtained by logging into the Astrill website with your Astrill username and password:
            https://www.astrill.com/member-zone/log-in
        If you are in China: Our alternative website is https://www.getastr.com
        Scroll down to "Router set-up" and click on "Install now".  The SSH command "eval wget ..." will be in the window.
        Make a copy. It will be pasted into the params.txt file described later.

        Login to router via Chrome, or equivalent, at address 192.168.50.1 with your Asus user name and password.
        The button for the Astrill VPN will be on the left.  Click and the Astrill interface will be brought up.  It will
        be necessary to disconnect VPN to load firmware.
        [TODO]  This description is for updating existing firmware. Need to include first time use instructions!

        Parameters for username, address, password, port, and the "eval wget ..." can be altered in /com.bob.router/files/params.txt.

        Create the params.txt file in /com.bob.router/files/ similar to:

            //   parameters for Asus RT-AX88U Pro with Astrill VPN
            user = admin            // Asus username
            password = admin
            host = 192.168.50.1
            port = 22
            loadAstrill = eval `wget -q -O - http://astroutercn.com/router/install/blahblahblah'

        where "admin" is the default value for the Asus router and should be replaced by your own values.
        The "loadAstrill" parameter is obtained by logging into your Astrill account at: https://www.astrill.com/member-zone/log-in
        and downloading the String for VPN on a router.  This will be: eval `wget -q -O - http://astroutercn.com/router/install/blahblahblah'
        Do not place a comment (//) on the "loadAstrill" line as even a cursory examination of the line above will make the reason clear!

        This app uses the library, JSch, to connect via ssh.  The jar file can be found at: http://www.jcraft.com/jsch/

