document.addEventListener('DOMContentLoaded', function () {
    var game = new Game2048();
    game.init();

    document.addEventListener('keydown', function (event) {
        if (!game.gameOver) {
            switch (event.key) {
                case 'ArrowUp':
                    game.move('up');
                    break;
                case 'ArrowDown':
                    game.move('down');
                    break;
                case 'ArrowLeft':
                    game.move('left');
                    break;
                case 'ArrowRight':
                    game.move('right');
                    break;
            }
        }
    });
});

function Game2048() {
    this.size = 4;
    this.grid = [];
    this.gameOver = false;
    this.timer = null;
    this.timeLeft = 60;
}

Game2048.prototype.init = function () {
    for (var i = 0; i < this.size; i++) {
        this.grid[i] = [];
        for (var j = 0; j < this.size; j++) {
            this.grid[i][j] = 0;
        }
    }
    this.addTile();
    this.addTile();
    this.updateGrid();
    this.gameOver = false;
    this.timeLeft = 60;
    document.getElementById('game-over-message').style.display = 'none';
    document.getElementById('timer').innerText = 'Time left: ' + this.timeLeft + ' seconds';

    clearInterval(this.timer);
    this.timer = setInterval(this.updateTimer.bind(this), 1000);
};

Game2048.prototype.updateTimer = function () {
    this.timeLeft--;
    document.getElementById('timer').innerText = 'Time left: ' + this.timeLeft + ' seconds';
    if (this.timeLeft <= 0) {
        clearInterval(this.timer);
        this.gameOver = true;
        document.getElementById('game-over-message').style.display = 'block';
        document.getElementById('game-over-message').innerHTML = '<h1>Game Over!</h1><button id="restart-button">Play Again</button>';
        document.getElementById('restart-button').addEventListener('click', this.init.bind(this));
    }
};

Game2048.prototype.addTile = function () {
    var emptyTiles = [];
    for (var i = 0; i < this.size; i++) {
        for (var j = 0; j < this.size; j++) {
            if (this.grid[i][j] === 0) {
                emptyTiles.push({x: i, y: j});
            }
        }
    }
    var randomTile = emptyTiles[Math.floor(Math.random() * emptyTiles.length)];
    this.grid[randomTile.x][randomTile.y] = Math.random() > 0.9 ? 4 : 2;
};

Game2048.prototype.move = function (direction) {
    var moved = false;

    switch (direction) {
        case 'up':
            for (var j = 0; j < this.size; j++) {
                var col = [];
                for (var i = 0; i < this.size; i++) {
                    if (this.grid[i][j] !== 0) col.push(this.grid[i][j]);
                }
                col = this.merge(col);
                for (var i = 0; i < this.size; i++) {
                    this.grid[i][j] = col[i] || 0;
                }
                if (col.length > 0) moved = true;
            }
            break;
        case 'down':
            for (var j = 0; j < this.size; j++) {
                var col = [];
                for (var i = this.size - 1; i >= 0; i--) {
                    if (this.grid[i][j] !== 0) col.push(this.grid[i][j]);
                }
                col = this.merge(col);
                for (var i = this.size - 1; i >= 0; i--) {
                    this.grid[i][j] = col[this.size - 1 - i] || 0;
                }
                if (col.length > 0) moved = true;
            }
            break;
        case 'left':
            for (var i = 0; i < this.size; i++) {
                var row = [];
                for (var j = 0; j < this.size; j++) {
                    if (this.grid[i][j] !== 0) row.push(this.grid[i][j]);
                }
                row = this.merge(row);
                for (var j = 0; j < this.size; j++) {
                    this.grid[i][j] = row[j] || 0;
                }
                if (row.length > 0) moved = true;
            }
            break;
        case 'right':
            for (var i = 0; i < this.size; i++) {
                var row = [];
                for (var j = this.size - 1; j >= 0; j--) {
                    if (this.grid[i][j] !== 0) row.push(this.grid[i][j]);
                }
                row = this.merge(row);
                for (var j = this.size - 1; j >= 0; j--) {
                    this.grid[i][j] = row[this.size - 1 - j] || 0;
                }
                if (row.length > 0) moved = true;
            }
            break;
    }

    if (moved) {
        this.addTile();
        this.updateGrid();
        if (!this.canMove()) {
            this.gameOver = true;
            clearInterval(this.timer);
            document.getElementById('game-over-message').style.display = 'block';
            document.getElementById('game-over-message').innerHTML = '<h1>Game Over!</h1><button id="restart-button">Play Again</button>';
            document.getElementById('restart-button').addEventListener('click', this.init.bind(this));
        }
    }
};

Game2048.prototype.merge = function (line) {
    for (var i = 0; i < line.length - 1; i++) {
        if (line[i] === line[i + 1]) {
            line[i] *= 2;
            line.splice(i + 1, 1);
        }
    }
    return line;
};

Game2048.prototype.updateGrid = function () {
    var gameContainer = document.getElementById('game-container');
    gameContainer.innerHTML = '';
    for (var i = 0; i < this.size; i++) {
        for (var j = 0; j < this.size; j++) {
            var tile = document.createElement('div');
            tile.className = 'tile tile-' + this.grid[i][j];
            tile.innerText = this.grid[i][j] === 0 ? '' : this.grid[i][j];
            gameContainer.appendChild(tile);
        }
    }
};

Game2048.prototype.canMove = function () {
    for (var i = 0; i < this.size; i++) {
        for (var j = 0; j < this.size; j++) {
            if (this.grid[i][j] === 0) return true;
            if (j < this.size - 1 && this.grid[i][j] === this.grid[i][j + 1]) return true;
            if (i < this.size - 1 && this.grid[i][j] === this.grid[i + 1][j]) return true;
        }
    }
    return false;
};

