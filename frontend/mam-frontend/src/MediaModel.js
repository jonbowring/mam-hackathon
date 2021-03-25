export class MediaModel {
	
	constructor(data) {
		this.id = data.id;
        this.fileName = data.fileName;
        this.fileExtension = data.fileExtension;
        this.mimeType = data.mimeType;
        this.fileSize = data.fileSize;
        this.fileEncoding = data.fileEncoding;
        this.url = data.url
	}

}