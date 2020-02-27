import React from 'react';
import ReactDOM from 'react-dom';
import SwaggerUI from 'swagger-ui-react';

import 'swagger-ui-react/swagger-ui.css';

const root = document.getElementById('root');

ReactDOM.render(
    <SwaggerUI {...root.dataset} />,
    root,
);

