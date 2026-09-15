#!/bin/bash
set -e

echo "Starting Hostel & Mess Tracker Web Service..."

# If Python 3 is available, serve a web dashboard on Railway's $PORT (default 8080)
PORT="${PORT:-8080}"

if command -v python3 &>/dev/null; then
    echo "Launching Python HTTP web server on port $PORT..."
    python3 -c "
import http.server
import socketserver
import os

PORT = int(os.environ.get('PORT', 8080))

HTML_CONTENT = '''<!DOCTYPE html>
<html lang=\"en\">
<head>
    <meta charset=\"UTF-8\">
    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">
    <title>Hostel & Mess Tracker</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
            background: linear-gradient(135deg, #1e1b4b 0%, #0f172a 100%);
            color: #f8fafc;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            margin: 0;
            padding: 20px;
        }
        .card {
            background: rgba(30, 41, 59, 0.85);
            backdrop-filter: blur(12px);
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 20px;
            padding: 40px;
            max-width: 600px;
            width: 100%;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
            text-align: center;
        }
        h1 {
            color: #38bdf8;
            margin-top: 0;
            font-size: 28px;
        }
        p {
            color: #cbd5e1;
            line-height: 1.6;
            font-size: 16px;
        }
        .badge {
            display: inline-block;
            background: #0284c7;
            color: white;
            padding: 6px 14px;
            border-radius: 9999px;
            font-size: 13px;
            font-weight: 600;
            margin-bottom: 20px;
        }
        .features {
            text-align: left;
            margin: 25px 0;
            background: rgba(15, 23, 42, 0.6);
            padding: 20px;
            border-radius: 12px;
        }
        .feature-item {
            display: flex;
            align-items: center;
            margin: 10px 0;
            font-size: 14px;
        }
        .dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: #38bdf8;
            margin-right: 12px;
        }
        .status-box {
            background: rgba(34, 197, 94, 0.15);
            border: 1px solid rgba(34, 197, 94, 0.4);
            color: #4ade80;
            padding: 12px;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 500;
            margin-top: 20px;
        }
    </style>
</head>
<body>
    <div class=\"card\">
        <div class=\"badge\">Android Native & Cloud Portal</div>
        <h1>Hostel & Mess Tracker</h1>
        <p>
            This repository contains the full <strong>Hostel &amp; Mess Management System</strong> 
            featuring QR-based check-ins, warden admin security controls, and student portals.
        </p>
        <div class=\"features\">
            <div class=\"feature-item\"><span class=\"dot\"></span>Bifurcated Admin &amp; Student Security Portals</div>
            <div class=\"feature-item\"><span class=\"dot\"></span>Real-time QR Code Entry/Exit Logging</div>
            <div class=\"feature-item\"><span class=\"dot\"></span>Mess Meal Consumption &amp; Diet Tracking</div>
            <div class=\"feature-item\"><span class=\"dot\"></span>Student Room Allocation &amp; Leave Approvals</div>
        </div>
        <div class=\"status-box\">
            Service is Online &amp; Healthy on Railway (Port ''' + str(PORT) + ''')
        </div>
    </div>
</body>
</html>'''

class Handler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html; charset=utf-8')
        self.end_headers()
        self.wfile.write(HTML_CONTENT.encode('utf-8'))

with socketserver.TCPServer(('', PORT), Handler) as httpd:
    print(f'Serving HTTP on 0.0.0.0 port {PORT}...')
    httpd.serve_forever()
"
else
    echo "Python not detected, waiting on port $PORT..."
    while true; do
        nc -l -p "$PORT" -e echo -e "HTTP/1.1 200 OK\r\nContent-Length: 2\r\n\r\nOK" || sleep 1
    done
fi
