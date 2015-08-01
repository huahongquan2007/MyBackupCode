/**
 * Created by quanhua92 on 8/1/15.
 */
var express = require('express'),
    api = require('./api'),
    app = express();

app.use(express.static('./public'))
    .use('/api', api)
    .get('*', function(req, res){
       res.sendfile('public/main.html');
    })
    .listen(3000);