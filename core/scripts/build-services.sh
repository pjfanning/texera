sbt clean dist
unzip workflow-compiling-service/target/universal/workflow-compiling-service-0.1.0.zip -d target/
rm workflow-compiling-service/target/universal/workflow-compiling-service-0.1.0.zip

unzip workflow-computing-unit-managing-service/target/universal/workflow-computing-unit-managing-service-0.1.0.zip -d target/
rm workflow-computing-unit-managing-service/target/universal/workflow-computing-unit-managing-service-0.1.0.zip

unzip amber/target/universal/texera-0.1-SNAPSHOT.zip -d amber/target/
rm amber/target/universal/texera-0.1-SNAPSHOT.zip


# Define the output file
output_file="amber/timestamp.txt"

# Get the current timestamp
current_timestamp=$(date +"%Y-%m-%d %H:%M:%S")

# Write the timestamp to the file
echo "$current_timestamp" > "$output_file"

echo "Timestamp written to $output_file"