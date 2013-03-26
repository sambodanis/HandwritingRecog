// Provide constants Player.X and Player.O for the two players, and constants
// Player.None and Player.Both to represent neither player or both players.
// A method is provided to find the other player from the current one.

enum Player
{
    X, O, None, Both;

    Player other()
    {
        if (this == X) return O;
        else if (this == O) return X;
        else throw new Error("Bug: other() called on None or Both");
    }
}
