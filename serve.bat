@echo off
REM Local preview: install dependencies, then start the MkDocs dev server.
REM Open http://127.0.0.1:8000 in a browser. Press Ctrl+C to stop.
pip install -r requirements.txt && mkdocs serve
pause
