from flask import Flask, render_template, request
from flask_bootstrap import Bootstrap

def create_app():
    app = Flask(__name__)
    Bootstrap(app)
    @app.route('/')
    def index():
        return render_template('index.html')
    
    @app.route('/browse')
    def browse():
        return render_template('browse.html')

    @app.route('/create', methods=["GET", "POST"])
    def create():
        if request.method == "POST":
            print(request.form)
            transcript = request.form['transcript']
            print(transcript)
            return render_template('create.html')
        return render_template('create.html')
    return app


if __name__ == '__main__':
    create_app().run(debug=True)


