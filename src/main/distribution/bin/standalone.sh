#!/bin/sh

DIRNAME=`dirname "$0"`
PROGNAME=`basename "$0"`
GREP="grep"

# Use the maximum available, or set MAX_FD != -1 to use that
MAX_FD="maximum"

# tell linux glibc how many memory pools can be created that are used by malloc
MALLOC_ARENA_MAX="${MALLOC_ARENA_MAX:-1}"
export MALLOC_ARENA_MAX

# OS specific support (must be 'true' or 'false').
cygwin=false;
darwin=false;
linux=false;
solaris=false;
freebsd=false;
other=false
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;

    Darwin*)
        darwin=true
        ;;
    FreeBSD)
        freebsd=true
        ;;
    Linux)
        linux=true
        ;;
    SunOS*)
        solaris=true
        ;;
    *)
        other=true
        ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "$APP_HOME" ] &&
        APP_HOME=`cygpath --unix "$APP_HOME"`
    [ -n "$JAVA_HOME" ] &&
        JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
    [ -n "$JAVAC_JAR" ] &&
        JAVAC_JAR=`cygpath --unix "$JAVAC_JAR"`
fi

# Setup APP_HOME
RESOLVED_APP_HOME=`cd "$DIRNAME/.." >/dev/null; pwd`
if [ "x$APP_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    APP_HOME=$RESOLVED_APP_HOME
else
 SANITIZED_APP_HOME=`cd "$APP_HOME"; pwd`
 if [ "$RESOLVED_APP_HOME" != "$SANITIZED_APP_HOME" ]; then
   echo ""
   echo "   WARNING:  APP_HOME may be pointing to a different installation - unpredictable results may occur."
   echo ""
   echo "             APP_HOME: $APP_HOME"
   echo ""
   sleep 2s
 fi
fi
export APP_HOME

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi

# determine the default base dir, if not set
if [ "x$APP_BASE_DIR" = "x" ]; then
   APP_BASE_DIR="$APP_HOME/standalone"
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    APP_HOME=`cygpath --path --windows "$APP_HOME"`
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
fi

# Display our environment
echo "========================================================================="
echo ""
echo "  Bootstrap Environment"
echo ""
echo "  APP_HOME: $APP_HOME"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "========================================================================="
echo ""

cd "$APP_HOME";
while true; do
   if [ "x$LAUNCH_APP_IN_BACKGROUND" = "x" ]; then
      # Execute the JVM in the foreground
      eval \"$JAVA\" \
         -jar \""$APP_HOME"/quarkus-run.jar\"
      APP_STATUS=$?
   else
      # Execute the JVM in the background
      eval \"$JAVA\" \
         -jar \""$APP_HOME"/quarkus-run.jar\"
      APP_PID=$!
      # Trap common signals and relay them to the process
      trap "kill -HUP  $APP_PID" HUP
      trap "kill -TERM $APP_PID" INT
      trap "kill -QUIT $APP_PID" QUIT
      trap "kill -PIPE $APP_PID" PIPE
      trap "kill -TERM $APP_PID" TERM
      if [ "x$APP_PIDFILE" != "x" ]; then
        echo $APP_PID > $APP_PIDFILE
      fi
      # Wait until the background process exits
      WAIT_STATUS=128
      while [ "$WAIT_STATUS" -ge 128 ]; do
         wait $APP_PID 2>/dev/null
         WAIT_STATUS=$?
         if [ "$WAIT_STATUS" -gt 128 ]; then
            SIGNAL=`expr $WAIT_STATUS - 128`
            SIGNAL_NAME=`kill -l $SIGNAL`
            echo "*** App AS process ($APP_PID) received $SIGNAL_NAME signal ***" >&2
         fi
      done
      if [ "$WAIT_STATUS" -lt 127 ]; then
         APP_STATUS=$WAIT_STATUS
      else
         APP_STATUS=0
      fi
      if [ "$APP_STATUS" -ne 10 ]; then
            # Wait for a complete shudown
            wait $APP_PID 2>/dev/null
      fi
      if [ "x$APP_PIDFILE" != "x" ]; then
            grep "$APP_PID" $APP_PIDFILE && rm $APP_PIDFILE
      fi
   fi
   if [ "$APP_STATUS" -eq 10 ]; then
      echo "Restarting application server..."
   else
      exit $APP_STATUS
   fi
done
