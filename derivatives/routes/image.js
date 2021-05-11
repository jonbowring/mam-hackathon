var express = require('express');
var Jimp = require('jimp');
var router = express.Router();


router.post('/resize', function(req, res, next) {

	var width = req.body.width != null ? req.body.width : Jimp.AUTO;
	var height = req.body.height != null ? req.body.height : Jimp.AUTO;

	Jimp.read(req.body.url)
		.then(image => {
			image.resize(width, height)
				.getBuffer(Jimp.AUTO, (err, buffer) => {
					
					if(err) {
						next(err);
					}
					
					res.writeHead(200, {
						'Content-Type': image.getMIME(),
						'Content-Length': buffer.length
					});
		
					res.end(buffer);
				});

		})
		.catch(err => {
			next(err);
		});

});

module.exports = router;
