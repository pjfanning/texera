#!/bin/bash

if jps -m | grep -q "TexeraWebApplication"; then
  echo "TexeraWebApplication is running."

  # Check if TexeraRunWorker is missing
  if ! jps -m | grep -q "TexeraRunWorker"; then
    echo "TexeraRunWorker is missing. Will check again after 30 seconds."
    sleep 30
    if ! jps -m | grep -q "TexeraRunWorker"; then
        echo "TexeraRunWorker is still missing. Restarting..."
        # Restart TexeraRunWorker
        cd "$(dirname "$0")"
        cd ../
        ./scripts/worker.sh >/dev/null
        echo "TexeraRunWorker restarted."
    else
      echo "TexeraRunWorker is running after the first check."
  else
    echo "TexeraRunWorker is already running."
  fi
else
  echo "TexeraWebApplication is not running."
fi

