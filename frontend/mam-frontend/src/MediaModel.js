import axios from 'axios';

export class MediaModel {
	
	constructor(data) {
		this.id = data.id;
        this.fileName = data.fileName;
        this.fileExtension = data.fileExtension;
        this.mimeType = data.mimeType;
        this.fileSize = data.fileSize;
        this.fileEncoding = data.fileEncoding;
        this.url = data.url;
		this.derivatives = data.derivatives !== null ? data.derivatives : null;
	}

	refresh() {
		let newMedia = null;
		
		axios({
			method: 'get',
			url: 'http://localhost:8080/media/' + this.id
		})
		.then((response) => {
			console.log(response);
		});

		return newMedia;
	}


	update(data) {
		axios({
			method: 'put',
			url: 'http://localhost:8080/media/' + this.id,
			data: data
		})
		.then((response) => {
			console.log(response)
		});
	}

	delete() {
		axios({
			method: 'delete',
			url: 'http://localhost:8080/media/' + this.id
		})
		.then((response) => {
			console.log(response)
		});
	}

}