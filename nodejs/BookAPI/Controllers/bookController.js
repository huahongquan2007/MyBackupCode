/**
 * Created by quanhua92 on 7/30/15.
 */

var bookController = function(Book){
    var post = function(req, res){
      var book = new Book(req.body);
      book.save();

      res.status(201);
      res.send(book);
    }

    var get = function(req, res){
        var query = {};
        if(req.query.genre){
            query.genre = req.query.genre;
        }
        Book.find(query, function (err, books) {
            if(err)
                res.status(500).send(err);
            else
                res.json(books);
        });
    };

    return {
        post: post,
        get: get
    }
};

module.exports = bookController;