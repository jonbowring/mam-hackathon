:: Change the working directory
pushd %1

:: Download the files
curl --config config.txt --create-dirs --silent

:: Zip the downloaded files
zip -r -j files.zip files