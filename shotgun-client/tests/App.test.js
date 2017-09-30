import 'react-native';
import React from 'react';
import App from '../App';
import Logger from '../viewserver-client/Logger';
import AppPage from './pages/App.page.js';

import renderer from 'react-test-renderer';

jasmine.DEFAULT_TIMEOUT_INTERVAL= 15000;

test('renders correctly',async () => {
    
    const tree = renderer.create(
        <App />
    );
    const appPage = new AppPage(tree);
    Logger.debug(tree.toJSON());
    await appPage.waitForLoaderToClear(50000,100);
    expect(tree).toMatchSnapshot();
});