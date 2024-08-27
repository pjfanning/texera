# release_port.py
import os
import sys

def release_port(port):
    cmd_find = 'netstat -aon | findstr %s' % (port)
    print('cmd_find:', cmd_find)
    res = os.popen(cmd_find).read()
    print('res:', res)
    if str(port) and 'LISTENING' in res:
        i = res.index('LISTENING')
        start = i + len('LISTENING') + 7
        end = res.find('\\n', start)
        pid = res[start:end].strip()
        print('pid:', pid)
        cmd_kill = 'taskkill -f -pid %s' % (pid)
        print('cmd_kill:', cmd_kill)
        os.popen(cmd_kill)
    else:
        print('This port is free to use')

if __name__ == "__main__":
    if len(sys.argv) > 1:
        port = int(sys.argv[1])
        release_port(port)
    else:
        print("Please provide a port number.")
