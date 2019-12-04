import socket
import ssl
import os
import sys
from struct import unpack


class server:
    """docstring for server"""
    def __init__(self):
        self.socket = None

    def listen(self, ip, port):
        self.serv = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.serv.bind((ip, port))
        self.serv.listen(5)

    def handle(self):
        while True:
            conn, addr = self.serv.accept()
            print 'Connecting from: ' + addr[0] + ':' + str(addr[1])
            from_client = ''
            while True:
                try:
                    bs = conn.recv(1024)
                    print bs
                    length = int(bs)
                    data = b''
                    while len(data) < length:
                        to_read = length - len(data)
                        data += conn.recv(
                            4096 if to_read > 4096 else to_read)
                        print length
                        print len(data)
                        # print(data)
                        fs = open(os.path.join('./recv','1'), "w")
                        fs.write(data)
                    fs.close
                    print("recv done")
                    # dats = ''
                    # chunk = ''
                    # while True:
                    #     chunk += conn.recv(1024)
                    #     print len(chunk)
                    #     if not chunk:
                    #         # Unreliable
                    #         break
                    #     else:
                    #         dats += chunk
                    #         fs = open(os.path.join('./recv','1.apk'), "w")
                    #         fs.write(dats)
                    #         fs.close
                finally:
                   conn.close()

if __name__ == '__main__':
    s = server()
    s.listen('192.168.1.10',7777)
    s.handle()
    main()