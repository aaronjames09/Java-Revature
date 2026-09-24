let name = "Aaron James";

function printMessage(name) {
    console.log(name + " is the best programmer ever!");
}

printMessage(name);

function getCurrentDate() {
    const date = new Date();

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
}

const button = document.getElementById("dateButton");

button.addEventListener("click", function () {

    let dateDiv = document.getElementById("dateDisplay");

    // Create the div if it doesn't already exist
    if (!dateDiv) {
        dateDiv = document.createElement("div");
        dateDiv.id = "dateDisplay";
        document.body.appendChild(dateDiv);
    }

    // Replace the existing date
    dateDiv.textContent = getCurrentDate();
});

const pokemonButton = document.getElementById("pokemonButton");

pokemonButton.addEventListener("click", async function () {

    const randomPokemonId = Math.floor(Math.random() * 151) + 1;

    try {
        const response = await fetch(
            `https://pokeapi.co/api/v2/pokemon/${randomPokemonId}`
        );

        const pokemon = await response.json();

        let pokemonDiv = document.getElementById("pokemonDisplay");

        if (!pokemonDiv) {
            pokemonDiv = document.createElement("div");
            pokemonDiv.id = "pokemonDisplay";
            document.body.appendChild(pokemonDiv);
        }

        pokemonDiv.innerHTML = `
            <p>
                <strong>${pokemon.name}</strong>
                <img 
                    src="${pokemon.sprites.front_default}" 
                    alt="${pokemon.name}"
                    width="50"
                    height="50"
                >
            </p>
        `;

    } catch (error) {
        console.error("Error fetching Pokémon:", error);
    }
});