import React from 'react';
import ProtoLoader from './viewserver-client/core/ProtoLoader';
import Client from "./viewserver-client/Client";

it('Loads protobuf Dtos', async () => {
  await ProtoLoader.loadAll();
  expect(ProtoLoader.Dto).toBeTruthy();
});

it('Connects to server', async () => {
  await ProtoLoader.loadAll();
  expect(ProtoLoader.Dto).toBeTruthy();
  this.client = new Client("ws://192.168.0.20:8080/");
  await this.client.connect();
  expect(this.client.connected).toBeTruthy();
});
