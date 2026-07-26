import './App.css';
import { Amplify } from 'aws-amplify';
import { Authenticator } from '@aws-amplify/ui-react';
// ✅ Utiliza el alias de exportación estándar
import '@aws-amplify/ui-react/styles.css'; 
import { fetchAuthSession } from 'aws-amplify/auth';
import { useState } from 'react';

//Configuración con los datos del día 7
Amplify.configure({
  Auth: {
    Cognito : {
      userPoolId: 'eu-west-1_EEv0Vgs6t',
      userPoolClientId: '1n8vpfcc26j48p2t8gnvhsb107'
    }
  }
});

function App() {
  const [apiResponse, setApiResponse] = useState("");
  const callStatusApi = async () => {
    try {
      //Obtener el idToken de la sesión activa en Cognito
      const session = await fetchAuthSession();
      const token = session.tokens?.idToken?.toString();

      if(!token) {
        throw new Error("No se encontró tolen activo");
      }

      //Realizar la llamada HTTP con el token en la cabecera Authorization
      const response = await fetch('https://brqfo5m4uj.execute-api.eu-west-1.amazonaws.com/prod/' , {
        method: 'GET',
        headers: {
          'Authorization': token,
          'Content-Type': 'application/json'
        }
      });
      const data = await response.json();
      setApiResponse(data.status);  //debería indicar 'Service is running
    } catch (error) {
      console.error("Error llamando a la api: ", error);
      setApiResponse("Error de conexión");
    }
  }
  return (
    <Authenticator>
      {({ signOut, user }) => (
        <main>
            <h1>Hola, {user.username}</h1>
            <button onClick={callStatusApi}>Llamar a la API</button>
            <p>Respuesta de la API: {apiResponse}</p>
            <button onClick={signOut}>Cerrar Sesión</button>
        </main>
      )}
    </Authenticator>
  );
}

export default App;
